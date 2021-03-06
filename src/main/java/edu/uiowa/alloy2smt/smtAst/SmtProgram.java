/*
 * This file is part of alloy2smt.
 * Copyright (C) 2018-2019  The University of Iowa
 *
 * @author Mudathir Mohamed, Paul Meng
 *
 */

package edu.uiowa.alloy2smt.smtAst;

import java.util.ArrayList;
import java.util.List;

public class SmtProgram extends SmtModel
{
    private final List<FunctionDeclaration>    functionDeclarations = new ArrayList<>();
    private final List<ConstantDeclaration>    constantDeclarations = new ArrayList<>();
    private final List<Assertion>              assertions           = new ArrayList<>();

    public void addFunctionDeclaration(FunctionDeclaration declaration)
    {
        if(declaration != null)
        {
            this.functionDeclarations.add(declaration);
        }
    }

    public void addConstantDeclaration(ConstantDeclaration constantDeclaration)
    {
        if(constantDeclaration != null)
        {
            this.constantDeclarations.add(constantDeclaration);
        }
    }

    public void addAssertion(Assertion assertion)
    {
        if(assertion != null)
        {
            this.assertions.add(assertion);
        }
    }

    public List<FunctionDeclaration> getFunctionDeclarations()
    {
        return this.functionDeclarations;
    }

    public List<ConstantDeclaration> getConstantDeclarations()
    {
        return this.constantDeclarations;
    }

    public List<Assertion> getAssertions()
    {
        return this.assertions;
    }
}
