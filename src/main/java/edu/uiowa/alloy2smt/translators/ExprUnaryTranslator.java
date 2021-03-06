/*
 * This file is part of alloy2smt.
 * Copyright (C) 2018-2019  The University of Iowa
 *
 * @author Mudathir Mohamed, Paul Meng
 *
 */

package edu.uiowa.alloy2smt.translators;

import edu.mit.csail.sdg.ast.*;
import edu.uiowa.alloy2smt.smtAst.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExprUnaryTranslator
{
    final ExprTranslator exprTranslator;
    final String valueOfUnaryIntTup;
    final String valueOfBinaryIntTup;
    final String valueOfTernaryIntTup;

    public ExprUnaryTranslator(ExprTranslator exprTranslator)
    {
        this.exprTranslator         = exprTranslator;
        this.valueOfUnaryIntTup     = exprTranslator.translator.valueOfUnaryIntTup.getName();
        this.valueOfBinaryIntTup    = exprTranslator.translator.valueOfBinaryIntTup.getName();
        this.valueOfTernaryIntTup   = exprTranslator.translator.valueOfTernaryIntTup.getName();
    }

    Expression translateExprUnary(ExprUnary exprUnary, Map<String, Expression> variablesScope)
    {
        switch (exprUnary.op)
        {
            case NOOP       : return translateNoop(exprUnary, variablesScope);
            case NO         : return translateNo(exprUnary, variablesScope);
            case SOME       : return translateSome(exprUnary, variablesScope);
            case ONE        : return translateOne(exprUnary, variablesScope);
            case ONEOF      : return translateOneOf(exprUnary, variablesScope);
            case LONE       : return translateLone(exprUnary, variablesScope);
            case CARDINALITY: throw new UnsupportedOperationException("CVC4 doesn't support cardinality operator with finite relations!");
            case TRANSPOSE  : return translateTranspose(exprUnary, variablesScope);
            case CLOSURE    : return translateClosure(exprUnary, variablesScope);
            case RCLOSURE   : return translateReflexiveClosure(exprUnary, variablesScope);
            case NOT        : return translateNot(exprUnary, variablesScope);
            case CAST2INT   : return translateCAST2INT(exprUnary, variablesScope);
            case CAST2SIGINT : return translateCAST2SIGINT(exprUnary, variablesScope);
            default:
            {
                throw new UnsupportedOperationException("Not supported yet: " + exprUnary.op);
            }
        }
    }
    
    private Expression translateCAST2INT(ExprUnary exprUnary, Map<String, Expression> variablesScope)
    {
        return exprTranslator.translateExpr(exprUnary.sub, variablesScope);
    }
    
    private Expression translateCAST2SIGINT(ExprUnary exprUnary, Map<String, Expression> variablesScope)
    {
        return exprTranslator.translateExpr(exprUnary.sub, variablesScope);
    }    

    private Expression translateNot(ExprUnary exprUnary, Map<String, Expression> variablesScope)
    {
        Expression expression   = exprTranslator.translateExpr(exprUnary.sub, variablesScope);
        Expression not          = new UnaryExpression(UnaryExpression.Op.NOT, expression);
        return not;
    }

    private Expression translateClosure(ExprUnary exprUnary, Map<String, Expression> variablesScope)
    {
        Expression      expression  = exprTranslator.translateExpr(exprUnary.sub, variablesScope);
        UnaryExpression closure     = new UnaryExpression(UnaryExpression.Op.TCLOSURE, expression);
        return closure;
    }

    private Expression translateReflexiveClosure(ExprUnary exprUnary, Map<String,Expression> variablesScope)
    {
        Expression          closure             = translateClosure(exprUnary, variablesScope);
        BinaryExpression    reflexiveClosure    = new BinaryExpression(closure, BinaryExpression.Op.UNION, exprTranslator.translator.atomIden.getConstantExpr());
        return reflexiveClosure;
    }

    private Expression translateTranspose(ExprUnary exprUnary, Map<String,Expression> variablesScope)
    {
        Expression      expression  = exprTranslator.translateExpr(exprUnary.sub, variablesScope);
        UnaryExpression transpose   = new UnaryExpression(UnaryExpression.Op.TRANSPOSE, expression);
        return transpose;
    }


    private Expression translateNoop(ExprUnary exprUnary, Map<String, Expression> variablesScope)
    {
        if(exprUnary.sub instanceof Sig)
        {

            // alloy built in signatures include: univ, none, iden
            if(((Sig) exprUnary.sub).builtin)
            {
                switch (((Sig) exprUnary.sub).label)
                {                    
                    case "univ": return exprTranslator.translator.atomUniv.getConstantExpr();
                    case "iden": return exprTranslator.translator.atomIden.getConstantExpr();
                    case "none": return exprTranslator.translator.atomNone.getConstantExpr();
                    case "Int": throw new UnsupportedOperationException("We do not support the built-in signature Int used in facts!");
                    default:
                        throw new UnsupportedOperationException();
                }
            }
            else
            {
                return exprTranslator.translator.signaturesMap.get(((Sig) exprUnary.sub)).getConstantExpr();
            }
        }

        if(exprUnary.sub instanceof Sig.Field)
        {
            return exprTranslator.translator.fieldsMap.get(((Sig.Field) exprUnary.sub)).getConstantExpr();
        }

        if(exprUnary.sub instanceof ExprVar)
        {
            String varName = ((ExprVar)exprUnary.sub).label;
            
            if(variablesScope.containsKey(varName))
            {
                Expression constExpr = variablesScope.get(varName);
                
                if(constExpr instanceof ConstantExpression)
                {
                    if(((ConstantExpression)constExpr).getDeclaration().getSort() == exprTranslator.translator.atomSort)
                    {
                        return new UnaryExpression(UnaryExpression.Op.SINGLETON, new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, constExpr));
                    }                    
                    else if(((ConstantExpression)constExpr).getDeclaration().getSort() == exprTranslator.translator.intSort)
                    {
                        return new UnaryExpression(UnaryExpression.Op.SINGLETON, new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, constExpr));
                    } 
                    else if(((ConstantExpression)constExpr).getDeclaration().getSort() instanceof TupleSort)
                    {
                        return new UnaryExpression(UnaryExpression.Op.SINGLETON, constExpr);
                    }                     
                }                
                return constExpr;
            }
            else
            {
                throw new RuntimeException("Something is wrong: we do not have variable in scope - " + varName);
            }            
        }
        
        return exprTranslator.translateExpr(exprUnary.sub, variablesScope);
    }
    
    private Expression tryAddingExistentialConstraint(Expression expr)
    {
        Expression finalExpr = expr;
        
        if(exprTranslator.translator.auxExpr != null)
        {
            finalExpr = new BinaryExpression(exprTranslator.translator.auxExpr, BinaryExpression.Op.AND, finalExpr);            
            finalExpr = new QuantifiedExpression(QuantifiedExpression.Op.EXISTS, exprTranslator.translator.existentialBdVars, finalExpr);
            exprTranslator.translator.auxExpr = null;
            exprTranslator.translator.existentialBdVars.clear();            
            
        }
        return finalExpr;
    }


    private Expression translateNo(ExprUnary exprUnary, Map<String, Expression> variablesScope)
    {
        int arity           = exprUnary.sub.type().arity();
        List<Sort> sorts    = exprTranslator.getExprSorts(exprUnary.sub);
        Expression set      = exprTranslator.translateExpr(exprUnary.sub, variablesScope);        
        
        List<Sort> elementSorts = new ArrayList<>();

        for(int i = 0; i < arity; i++)
        {
            elementSorts.add(sorts.get(i));
        }
        Expression eqExpr = new BinaryExpression(set, BinaryExpression.Op.EQ, 
                                    new UnaryExpression(UnaryExpression.Op.EMPTYSET, new SetSort(new TupleSort(elementSorts))));         
        return tryAddingExistentialConstraint(eqExpr);
    }

    private Expression translateSome(ExprUnary exprUnary, Map<String,Expression> variablesScope)
    {
        int arity           = exprUnary.sub.type().arity();
        List<Sort> sorts    = exprTranslator.getExprSorts(exprUnary.sub);
        Expression someRel  = exprTranslator.translateExpr(exprUnary.sub, variablesScope);  
        List<BoundVariableDeclaration>  bdVars      = new ArrayList<>();
        List<Expression>                bdVarExprs  = new ArrayList<>();        
        
        for(Sort sort : sorts)
        {
            String name = TranslatorUtils.getNewName();
            BoundVariableDeclaration bdVar;
            Expression bdVarExpr;
            
            if(sort instanceof IntSort)
            {
                bdVar = new BoundVariableDeclaration(name, exprTranslator.translator.unaryIntTup);                
                bdVarExpr = mkTupleSelExpr(mkUnaryIntTupValue(bdVar.getConstantExpr()), 0);
            }
            else
            {
                bdVar = new BoundVariableDeclaration(name, exprTranslator.translator.atomSort);
                bdVarExpr = bdVar.getConstantExpr();
            }
            bdVars.add(bdVar);
            bdVarExprs.add(bdVarExpr);
        }
        Expression bdVarTupExpr     = ExprUnaryTranslator.this.mkOneTupleExprOutofAtoms(bdVarExprs);
        Expression bodyExpr         = new BinaryExpression(bdVarTupExpr, BinaryExpression.Op.MEMBER, someRel);
        
        // If the expression is a binary or ternary field, we need to make sure 
        // there exists a var of type binaryIntTup such that the integer tuple equals to the bdVarTupExpr.
        bodyExpr = addConstraintForBinAndTerIntRel(bdVarTupExpr, exprUnary.sub, bodyExpr);
        

        QuantifiedExpression exists = new QuantifiedExpression(QuantifiedExpression.Op.EXISTS, bdVars, bodyExpr);
        return tryAddingExistentialConstraint(exists);
    }    

    private Expression translateOne(ExprUnary exprUnary, Map<String,Expression> variablesScope)
    {
        int arity           = exprUnary.sub.type().arity();
        List<Sort> sorts    = exprTranslator.getExprSorts(exprUnary.sub);
        Expression set      = exprTranslator.translateExpr(exprUnary.sub, variablesScope);  
        List<BoundVariableDeclaration>  bdVars      = new ArrayList<>();
        List<Expression>                bdVarExprs  = new ArrayList<>();
        
        for(Sort sort : sorts)
        {
            String name = TranslatorUtils.getNewName();
            BoundVariableDeclaration bdVar;
            Expression bdVarExpr;
            
            if(sort instanceof IntSort)
            {
                bdVar = new BoundVariableDeclaration(name, exprTranslator.translator.unaryIntTup);                
                bdVarExpr = mkTupleSelExpr(mkUnaryIntTupValue(bdVar.getConstantExpr()), 0);
            }
            else
            {
                bdVar = new BoundVariableDeclaration(name, exprTranslator.translator.atomSort);
                bdVarExpr = bdVar.getConstantExpr();
            }
            bdVars.add(bdVar);
            bdVarExprs.add(bdVarExpr);
        }
        Expression bdVarTupExpr = mkOneTupleExprOutofAtoms(bdVarExprs);
        Expression bdVarSetExpr = mkSingleton(bdVarTupExpr);
        Expression bodyExpr     = new BinaryExpression(bdVarSetExpr, BinaryExpression.Op.EQ, set);
        bodyExpr = addConstraintForBinAndTerIntRel(bdVarTupExpr, exprUnary.sub, bodyExpr);
        
        QuantifiedExpression exists = new QuantifiedExpression(QuantifiedExpression.Op.EXISTS, bdVars, bodyExpr);
        return tryAddingExistentialConstraint(exists);
    }
    
    private Expression translateOneOf(ExprUnary exprUnary, Map<String,Expression> variablesScope)
    {
        Expression set = exprTranslator.translateExpr(exprUnary.sub, variablesScope);

        return set;
    }    

    private Expression translateLone(ExprUnary exprUnary, Map<String,Expression> variablesScope)
    {
        int arity           = exprUnary.sub.type().arity();
        List<Sort> sorts    = exprTranslator.getExprSorts(exprUnary.sub);
        Expression set      = exprTranslator.translateExpr(exprUnary.sub, variablesScope);  
        List<BoundVariableDeclaration>  bdVars      = new ArrayList<>();
        List<Expression>                bdVarExprs  = new ArrayList<>();
        
        for(Sort sort : sorts)
        {
            String name = TranslatorUtils.getNewName();
            BoundVariableDeclaration bdVar;
            Expression bdVarExpr;
            
            if(sort instanceof IntSort)
            {
                bdVar = new BoundVariableDeclaration(name, exprTranslator.translator.unaryIntTup);                
                bdVarExpr = mkTupleSelExpr(mkUnaryIntTupValue(bdVar.getConstantExpr()), 0);
            }
            else
            {
                bdVar = new BoundVariableDeclaration(name, exprTranslator.translator.atomSort);
                bdVarExpr = bdVar.getConstantExpr();
            }
            bdVars.add(bdVar);
            bdVarExprs.add(bdVarExpr);
        }
        Expression bdVarTupExpr = mkOneTupleExprOutofAtoms(bdVarExprs);
        Expression bdVarSetExpr = mkSingleton(bdVarTupExpr);
        Expression bodyExpr     = new BinaryExpression(set, BinaryExpression.Op.SUBSET, bdVarSetExpr);
        bodyExpr = addConstraintForBinAndTerIntRel(bdVarTupExpr, exprUnary.sub, bodyExpr);
        
        QuantifiedExpression exists = new QuantifiedExpression(QuantifiedExpression.Op.EXISTS, bdVars, bodyExpr);
        return tryAddingExistentialConstraint(exists);
    }
    
    public MultiArityExpression mkTupleExpr(BoundVariableDeclaration bdVarDecl)
    {
        return new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, bdVarDecl.getConstantExpr());
    }
    
    public MultiArityExpression mkOneTupleExprOutofAtoms(Expression ... exprs)
    {
        return new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, exprs);
    } 
    
    public MultiArityExpression mkOneTupleExprOutofAtoms(List<Expression> exprs)
    {
        return new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, exprs);
    }     
    
    public MultiArityExpression mkTupleExprOutofUnaryTuples(Expression ... exprs)
    {
        List<Expression> atomExprs = new ArrayList<>();
        
        for(Expression e : exprs)
        {
            atomExprs.add(new BinaryExpression(new IntConstant(0), BinaryExpression.Op.TUPSEL, e));
        }
        return new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, atomExprs);
    }     
    
    public UnaryExpression mkSingleton(BoundVariableDeclaration bdVarDecl)
    {
        return new UnaryExpression(UnaryExpression.Op.SINGLETON, mkTupleExpr(bdVarDecl));
    } 
    
    public UnaryExpression mkSingletonOutOfAtomExpr(Expression expr)
    {
        return new UnaryExpression(UnaryExpression.Op.SINGLETON, mkTupleExpr(expr));
    }   
    
    public Expression mkTupleExpr(Expression expr)
    {
        return new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, expr);
    }
    
    public UnaryExpression mkSingleton(BoundVariableDeclaration ... bdVarDecls)
    {
        return new UnaryExpression(UnaryExpression.Op.SINGLETON, mkTupleExpr(bdVarDecls));
    } 
    
    public UnaryExpression mkSingleton(MultiArityExpression tuple)
    {
        return new UnaryExpression(UnaryExpression.Op.SINGLETON, tuple);
    }  
    
    public UnaryExpression mkSingleton(Expression tuple)
    {
        return new UnaryExpression(UnaryExpression.Op.SINGLETON, tuple);
    }      
    
    public MultiArityExpression mkTupleExpr(BoundVariableDeclaration ... bdVarDecls)
    {
        List<Expression> constExprs = new ArrayList<>();
        for(BoundVariableDeclaration varDecl : bdVarDecls)
        {
            constExprs.add(varDecl.getConstantExpr());
        }
        return new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, constExprs);
    } 
    
    public MultiArityExpression mkTupleExpr(List<BoundVariableDeclaration> bdVarDecls)
    {
        List<Expression> constExprs = new ArrayList<>();
        for(BoundVariableDeclaration varDecl : bdVarDecls)
        {
            constExprs.add(varDecl.getConstantExpr());
        }
        return new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, constExprs);
    }  
    
    public Expression mkTupleSelExpr(Expression expr, int index)
    {
        return new BinaryExpression(new IntConstant(index), BinaryExpression.Op.TUPSEL, expr);
    }
    
    public Expression mkUnaryIntTupValue(Expression expr)
    {
        return new FunctionCallExpression(this.valueOfUnaryIntTup, expr);
    }
    
    public Expression mkBinaryIntTupValue(Expression expr)
    {
        return new FunctionCallExpression(this.valueOfBinaryIntTup, expr);
    }

    public Expression mkTernaryIntTupValue(Expression expr)
    {
        return new FunctionCallExpression(this.valueOfTernaryIntTup, expr);
    }    
   
    
    
    private boolean isBinaryIntField(Expr exprUnary)
    {
        if((exprUnary instanceof ExprUnary))
        {
            if(((ExprUnary)exprUnary).op == ExprUnary.Op.NOOP)
            {
                if((((ExprUnary)exprUnary).sub instanceof Sig.Field))
                {
                    List<Sort> sorts = exprTranslator.getExprSorts(exprUnary);
                    if(sorts.size() == 2)
                    {
                        return (sorts.get(0) instanceof IntSort) && (sorts.get(1) instanceof IntSort);
                    }                    
                }
            }
        }
        return false;
    }  
    
    private boolean isTernaryIntField(Expr exprUnary)
    {
        if((exprUnary instanceof ExprUnary) && ((ExprUnary)exprUnary).op == ExprUnary.Op.NOOP && (((ExprUnary)exprUnary).sub instanceof Sig.Field))
        {
            List<Sort> sorts = exprTranslator.getExprSorts(exprUnary);
            if(sorts.size() == 3)
            {
                return (sorts.get(0) instanceof IntSort) && (sorts.get(1) instanceof IntSort)
                        && (sorts.get(2) instanceof IntSort);
            }
        }
        return false;
    }  

    private Expression addConstraintForBinAndTerIntRel(Expression bdVarTupExpr, Expr exprUnary, Expression bodyExpr)
    {
        Expression finalExpr = bodyExpr;
        if(isBinaryIntField(exprUnary))
        {            
            BoundVariableDeclaration bdBinIntTup = new BoundVariableDeclaration(TranslatorUtils.getNewName(), exprTranslator.translator.binaryIntTup);
            Expression eq = new BinaryExpression(mkBinaryIntTupValue(bdBinIntTup.getConstantExpr()), BinaryExpression.Op.EQ, bdVarTupExpr);
            QuantifiedExpression quantEq = new QuantifiedExpression(QuantifiedExpression.Op.EXISTS, eq, bdBinIntTup);            
            finalExpr = new BinaryExpression(bodyExpr, BinaryExpression.Op.AND, quantEq);
        }
        else if(isTernaryIntField(exprUnary))
        {            
            BoundVariableDeclaration bdTernaryIntTup = new BoundVariableDeclaration(TranslatorUtils.getNewName(), exprTranslator.translator.ternaryIntTup);
            Expression eq = new BinaryExpression(mkTernaryIntTupValue(bdTernaryIntTup.getConstantExpr()), BinaryExpression.Op.EQ, bdVarTupExpr);
            QuantifiedExpression quantEq = new QuantifiedExpression(QuantifiedExpression.Op.EXISTS, eq, bdTernaryIntTup);
            finalExpr = new BinaryExpression(bodyExpr, BinaryExpression.Op.AND, quantEq);
        }    
        return finalExpr;
    }    
}
