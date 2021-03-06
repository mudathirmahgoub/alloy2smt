/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uiowa.alloy2smt.smtAst;

import edu.uiowa.alloy2smt.printers.SmtAstVisitor;

/**
 *
 * @author Paul Meng
 */
public class ITEExpression extends Expression
{
    private final Expression                  condExpr;
    private final Expression                  thenExpr;
    private final Expression                  elseExpr;
    private final Op                          op = Op.ITE;
    
    public ITEExpression(Expression condExpr, Expression thenExpr, Expression elseExpr)
    {        
        this.condExpr = condExpr;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    
    public Expression getCondExpression()
    {
        return this.condExpr;
    }
    
    public Expression getThenExpression()
    {
        return this.thenExpr;
    }    
    
    public Expression getElseExpression()
    {
        return this.elseExpr;
    }        

    public Op getOp()
    {
        return this.op;
    }

    @Override
    public void accept(SmtAstVisitor visitor) {
        visitor.visit(this);
    }
    
    public enum Op 
    {        
        ITE ("ite");    

        private final String opStr;

        private Op(String op) 
        {
            this.opStr = op;
        }

        @Override
        public String toString() 
        {
            return this.opStr;
        }        
    }     


}
