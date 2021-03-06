/*
 * This file is part of alloy2smt.
 * Copyright (C) 2018-2019  The University of Iowa
 *
 * @author Mudathir Mohamed, Paul Meng
 *
 */

package edu.uiowa.alloy2smt.smtAst;

import edu.uiowa.alloy2smt.printers.SmtAstVisitor;

public class StringSort extends Sort
{
    public StringSort()
    {
        super("String", 0);
    }
    @Override
    public void accept(SmtAstVisitor visitor) {
        visitor.visit(this);
    }
}
