/*
 * This file is part of alloy2smt.
 * Copyright (C) 2018-2019  The University of Iowa
 *
 * @author Mudathir Mohamed, Paul Meng
 *
 */

package edu.uiowa.alloy2smt.smtAst;

public class Assertion
{
    private Expression expression;

    public Assertion(Expression expression)
    {
        this.expression = expression;
    }

    public Expression getExpression()
    {
        return this.expression;
    }
}