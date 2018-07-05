/*
 * This file is part of alloy2smt.
 * Copyright (C) 2018-2019  The University of Iowa
 *
 * @author Mudathir Mohamed, Paul Meng
 *
 */

package edu.uiowa.alloy2smt.translators;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.ast.*;
import edu.mit.csail.sdg.parser.CompModule;
import edu.uiowa.alloy2smt.Alloy2SMTLogger;
import edu.uiowa.alloy2smt.Utils;
import edu.uiowa.alloy2smt.smtAst.*;

import java.util.*;
import java.util.stream.Collectors;

public class Alloy2SMTTranslator
{
    public final SMTProgram smtProgram;
    
    final Alloy2SMTLogger LOGGER = new Alloy2SMTLogger("Alloy2SMTTranslator");
    
    final String                    atom;
    final CompModule                alloyModel;
    final List<Sig>                 reachableSigs;
    final List<Sig>                 topLevelSigs;
    final SetSort                   setOfUnaryAtomSort;
    final SetSort                   setOfBinaryAtomSort;
    final UninterpretedSort         atomSort;
    final TupleSort                 unaryAtomSort;
    final TupleSort                 binaryAtomSort;

    Map<Sig,FunctionDeclaration>        signaturesMap   = new HashMap<>();
    Map<Sig.Field,FunctionDeclaration>  fieldsMap       = new HashMap<>();

    public Alloy2SMTTranslator(CompModule alloyModel)
    {
        this.smtProgram             = new SMTProgram();
        
        this.atom                   = "Atom";
        this.alloyModel             = alloyModel;
        this.reachableSigs          = new ArrayList<>();
        this.topLevelSigs           = new ArrayList<>();
        this.atomSort               = new UninterpretedSort(this.atom);
        this.unaryAtomSort          = new TupleSort(this.atomSort);
        this.binaryAtomSort         = new TupleSort(this.atomSort, this.atomSort);
        this.setOfUnaryAtomSort     = new SetSort(this.unaryAtomSort);
        this.setOfBinaryAtomSort    = new SetSort(this.binaryAtomSort);
    }

    public SMTProgram execute()
    {        
        SignatureTranslator signatureTranslator = new SignatureTranslator(this);
        signatureTranslator.translate();
        translateFacts();
        return this.smtProgram;
    }

    private void translateFacts()
    {
        for (Pair<String, Expr> pair :this.alloyModel.getAllFacts() )
        {
            translateFact(pair.a, pair.b);
        }
    }

    private void translateFact(String factName, Expr factExpr)
    {
        Map<String, ConstantExpression> variablesScope = new HashMap<>();
        Expression expression = translateExpr(factExpr, variablesScope);
        this.smtProgram.addAssertion(new Assertion(factName, expression));
    }

    private Expression translateExpr(Expr expr, Map<String, ConstantExpression> variablesScope)
    {
        if(expr instanceof ExprUnary)
        {
            return translateExprUnary((ExprUnary) expr, variablesScope);
        }
        if(expr instanceof ExprBinary)
        {
            return translateExprBinary((ExprBinary) expr, variablesScope);
        }
        if(expr instanceof ExprQt)
        {
            return translateExprQt((ExprQt) expr, variablesScope);
        }
        throw new UnsupportedOperationException();
    }

    private Expression translateExprBinary(ExprBinary expr, Map<String, ConstantExpression> variablesScope)
    {
        switch (expr.op)
        {
            case ARROW              : throw new UnsupportedOperationException();
            case ANY_ARROW_SOME     : throw new UnsupportedOperationException();
            case ANY_ARROW_ONE      : throw new UnsupportedOperationException();
            case ANY_ARROW_LONE     : throw new UnsupportedOperationException();
            case SOME_ARROW_ANY     : throw new UnsupportedOperationException();
            case SOME_ARROW_SOME    : throw new UnsupportedOperationException();
            case SOME_ARROW_ONE     : throw new UnsupportedOperationException();
            case SOME_ARROW_LONE    : throw new UnsupportedOperationException();
            case ONE_ARROW_ANY      : throw new UnsupportedOperationException();
            case ONE_ARROW_SOME     : throw new UnsupportedOperationException();
            case ONE_ARROW_ONE      : throw new UnsupportedOperationException();
            case ONE_ARROW_LONE     : throw new UnsupportedOperationException();
            case LONE_ARROW_ANY     : throw new UnsupportedOperationException();
            case LONE_ARROW_SOME    : throw new UnsupportedOperationException();
            case LONE_ARROW_ONE     : throw new UnsupportedOperationException();
            case LONE_ARROW_LONE    : throw new UnsupportedOperationException();
            case ISSEQ_ARROW_LONE   : throw new UnsupportedOperationException();
            case JOIN               : return translateJoin(expr, variablesScope);
            case DOMAIN             : throw new UnsupportedOperationException();
            case RANGE              : throw new UnsupportedOperationException();
            case INTERSECT          : throw new UnsupportedOperationException();
            case PLUSPLUS           : throw new UnsupportedOperationException();
            case PLUS               : return translatePlus(expr, variablesScope);
            case IPLUS              : throw new UnsupportedOperationException();
            case MINUS              : throw new UnsupportedOperationException();
            case IMINUS             : throw new UnsupportedOperationException();
            case MUL                : throw new UnsupportedOperationException();
            case DIV                : throw new UnsupportedOperationException();
            case REM                : throw new UnsupportedOperationException();
            case EQUALS             : throw new UnsupportedOperationException();
            case NOT_EQUALS         : throw new UnsupportedOperationException();
            case IMPLIES            : throw new UnsupportedOperationException();
            case LT                 : throw new UnsupportedOperationException();
            case LTE                : throw new UnsupportedOperationException();
            case GT                 : throw new UnsupportedOperationException();
            case GTE                : throw new UnsupportedOperationException();
            case NOT_LT             : throw new UnsupportedOperationException();
            case NOT_LTE            : throw new UnsupportedOperationException();
            case NOT_GT             : throw new UnsupportedOperationException();
            case NOT_GTE            : throw new UnsupportedOperationException();
            case SHL                : throw new UnsupportedOperationException();
            case SHA                : throw new UnsupportedOperationException();
            case SHR                : throw new UnsupportedOperationException();
            case IN                 : throw new UnsupportedOperationException();
            case NOT_IN             : throw new UnsupportedOperationException();
            case AND                : throw new UnsupportedOperationException();
            case OR                 : throw new UnsupportedOperationException();
            case IFF                : throw new UnsupportedOperationException();
            default                 : throw new UnsupportedOperationException();
        }
    }

    private Expression translatePlus(ExprBinary expr, Map<String, ConstantExpression> variablesScope)
    {
        Expression left = translateExpr(expr.left, variablesScope);
        Expression right = translateExpr(expr.right, variablesScope);

        if(left instanceof ConstantExpression &&
                ((ConstantExpression)left).getDeclaration() instanceof BoundVariableDeclaration)
        {
            left = getSingleton((ConstantExpression) left);
        }
        if(right instanceof ConstantExpression &&
                ((ConstantExpression)right).getDeclaration() instanceof BoundVariableDeclaration)
        {
            right = getSingleton((ConstantExpression) right);
        }

        BinaryExpression union = new BinaryExpression(left, BinaryExpression.Op.UNION, right);
        return union;
    }

    private Expression translateJoin(ExprBinary expr, Map<String, ConstantExpression> variablesScope)
    {
        Expression          left    = translateExpr(expr.left, variablesScope);
        Expression          right   = translateExpr(expr.right, variablesScope);

        if(left instanceof ConstantExpression &&
            ((ConstantExpression)left).getDeclaration() instanceof BoundVariableDeclaration)
        {
            left = getSingleton((ConstantExpression) left);
        }
        if(right instanceof ConstantExpression &&
            ((ConstantExpression)right).getDeclaration() instanceof BoundVariableDeclaration)
        {
            right = getSingleton((ConstantExpression) right);
        }
        BinaryExpression    join    = new BinaryExpression(left, BinaryExpression.Op.JOIN, right);
        return join;
    }

    private Expression getSingleton(ConstantExpression constantExpression)
    {
        MultiArityExpression tuple      = new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, constantExpression);
        UnaryExpression      singleton  = new UnaryExpression(UnaryExpression.Op.SINGLETON, tuple);
        return singleton;
    }

    private Expression translateExprUnary(ExprUnary exprUnary, Map<String, ConstantExpression> variablesScope)
    {
        switch (exprUnary.op)
        {
            case NOOP   : return translateNoop(exprUnary, variablesScope);
            case NO     : return translateNo(exprUnary, variablesScope);
            default:
            {
                throw new UnsupportedOperationException("Not supported yet");
            }
        }
    }



    private Expression translateNoop(ExprUnary exprUnary, Map<String, ConstantExpression> variablesScope)
    {
        if(exprUnary.sub instanceof Sig)
        {
            return this.signaturesMap.get(exprUnary.sub).getConstantExpr();
        }

        if(exprUnary.sub instanceof Sig.Field)
        {
            return this.fieldsMap.get(exprUnary.sub).getConstantExpr();
        }

        if(exprUnary.sub instanceof ExprVar)
        {
            return variablesScope.get(((ExprVar)exprUnary.sub).label);
        }

        if(exprUnary.sub instanceof ExprQt)
        {
            return translateExprQt((ExprQt) exprUnary.sub, variablesScope);
        }

        if(exprUnary.sub instanceof ExprUnary)
        {
            return translateExprUnary((ExprUnary) exprUnary.sub, variablesScope);
        }

        if(exprUnary.sub instanceof ExprList)
        {
            return translateExprList((ExprList) exprUnary.sub, variablesScope);
        }

        throw new UnsupportedOperationException();
    }

    private Expression translateExprList(ExprList exprList, Map<String, ConstantExpression> variablesScope)
    {
        switch (exprList.op)
        {
            case AND    : return translateExprListToBinaryExpressions(BinaryExpression.Op.AND, exprList, variablesScope);
            default     : throw new UnsupportedOperationException();
        }
    }

    private Expression translateExprListToBinaryExpressions(BinaryExpression.Op op, ExprList exprList, Map<String, ConstantExpression> variablesScope)
    {
        //ToDo: review the case of nested variable scopes
        Expression left         = translateExpr(exprList.args.get(0), variablesScope);
        Expression right        = translateExpr(exprList.args.get(1), variablesScope);
        BinaryExpression result = new BinaryExpression(left, op, right);


        for(int i = 2; i < exprList.args.size(); i++)
        {
            Expression expr     = translateExpr(exprList.args.get(i), variablesScope);
            //ToDo: review right associativity
            result              = new BinaryExpression(result, op, expr);
        }

        return result;
    }

    private Expression translateExprQt(ExprQt exprQt, Map<String, ConstantExpression> variablesScope)
    {
        Map<BoundVariableDeclaration, FunctionDeclaration> boundVariables = new HashMap<>();
        for (Decl decl: exprQt.decls)
        {
            FunctionDeclaration functionDeclaration = getFunctionDeclaration(decl);
            for (ExprHasName name: decl.names)
            {
                BoundVariableDeclaration boundVariable = new BoundVariableDeclaration(name.label, this.atomSort);
                variablesScope.put(name.label, boundVariable.getConstantExpr());
                boundVariables.put(boundVariable, functionDeclaration);
            }
        }

        Expression           expression             = translateExpr(exprQt.sub, variablesScope);

        switch (exprQt.op)
        {
            case ALL: return  translateAllQuantifier(boundVariables, expression);
            default: throw new UnsupportedOperationException();
        }
    }

    private QuantifiedExpression translateAllQuantifier(Map<BoundVariableDeclaration, FunctionDeclaration> boundVariables, Expression expression)
    {
        if(boundVariables.size() == 1)
        {
            BinaryExpression member = getMemberExpression(boundVariables, 0);
            expression              = new BinaryExpression(member, BinaryExpression.Op.IMPLIES, expression);
        }
        else if (boundVariables.size() > 1)
        {
            Expression member1 = getMemberExpression(boundVariables, 0);
            Expression member2 = getMemberExpression(boundVariables, 1);

            BinaryExpression and = new BinaryExpression(member1, BinaryExpression.Op.AND, member2);

            for(int i = 2; i < boundVariables.size(); i++)
            {
                Expression member   = getMemberExpression(boundVariables, i);
                and                 = new BinaryExpression(and, BinaryExpression.Op.AND, member);
            }

            expression              = new BinaryExpression(and, BinaryExpression.Op.IMPLIES, expression);
        }

        QuantifiedExpression quantifiedExpression = new QuantifiedExpression(QuantifiedExpression.Op.FORALL, new ArrayList<>(boundVariables.keySet()), expression);
        return quantifiedExpression;
    }

    private BinaryExpression getMemberExpression(Map<BoundVariableDeclaration, FunctionDeclaration> boundVariables, int index)
    {
        BoundVariableDeclaration    boundVariable       = (new ArrayList<>(boundVariables.keySet())).get(index);
        FunctionDeclaration         functionDeclaration = boundVariables.get(boundVariable);
        MultiArityExpression        tuple               = new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, boundVariable.getConstantExpr());
        return new BinaryExpression(tuple, BinaryExpression.Op.MEMBER, functionDeclaration.getConstantExpr());
    }

    private FunctionDeclaration getFunctionDeclaration(Decl decl)
    {
        if(decl.expr instanceof ExprUnary)
        {
            Expr expr = (((ExprUnary) decl.expr).sub);
            if(expr instanceof ExprUnary)
            {
                if(((ExprUnary) expr).sub instanceof Sig)
                {
                    Sig sig = (Sig) ((ExprUnary) expr).sub;
                    return this.signaturesMap.get(sig);
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    private Expression translateNo(ExprUnary exprUnary, Map<String, ConstantExpression> variablesScope)
    {
        BoundVariableDeclaration variable = new BoundVariableDeclaration(Utils.getNewName(), this.atomSort);
        MultiArityExpression tuple = new MultiArityExpression(MultiArityExpression.Op.MKTUPLE, variable.getConstantExpr());
        Expression set = translateExpr(exprUnary.sub, variablesScope);
        BinaryExpression member = new BinaryExpression(tuple, BinaryExpression.Op.MEMBER, set);
        Expression expression = new UnaryExpression(UnaryExpression.Op.NOT, member);
        QuantifiedExpression forall = new QuantifiedExpression(QuantifiedExpression.Op.FORALL, expression, variable);
        return forall;
    }

    private void mkAndAddBinaryAtomRelation(String varName) 
    {
        if(varName != null) 
        {
            this.smtProgram.addFunctionDeclaration(new FunctionDeclaration(varName, this.setOfBinaryAtomSort));
        }        
    }    
    
    private void addToVarDecl(FunctionDeclaration varDecl)
    {
        if(varDecl != null) 
        {
            this.smtProgram.addFunctionDeclaration(varDecl);
        }
        else 
        {
            LOGGER.printSevere("Try to add a null variable declaration!");
        }
    }
    
    private void addToFcnDecl(FunctionDeclaration fcnDecl) 
    {
        if(fcnDecl != null) 
        {
            this.smtProgram.addFcnDecl(fcnDecl);
        }
        else 
        {
            LOGGER.printSevere("Try to add a null function declaration!");
        }
    }    
    
    private void addToFcnDef(FunctionDefinition fcnDef) 
    {
        if(fcnDef != null) 
        {
            this.smtProgram.addFcnDef(fcnDef);
        }
        else 
        {
            LOGGER.printSevere("Try to add a null function declaration!");
        }
    }    
}
