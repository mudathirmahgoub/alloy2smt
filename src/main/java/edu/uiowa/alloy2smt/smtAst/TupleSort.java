/*
 * This file is part of alloy2smt.
 * Copyright (C) 2018-2019  The University of Iowa
 *
 * @author Mudathir Mohamed, Paul Meng
 *
 */

package edu.uiowa.alloy2smt.smtAst;

import edu.uiowa.alloy2smt.printers.SmtAstVisitor;
import java.util.Arrays;
import java.util.List;

public class TupleSort extends Sort
{
    public List<Sort> elementSorts;

    public TupleSort(List<Sort> elementSorts)
    {
        super("Tuple", 0);
        this.elementSorts = elementSorts;
    }

    public TupleSort(Sort ... elementSorts)
    {
        this(Arrays.asList(elementSorts));
    }

    @Override
    public String toString() 
    {
        String result = "(Tuple ";
        
        for(int i = 0; i < this.elementSorts.size(); ++i) 
        {
            result += this.elementSorts.get(i).toString();
            
            if(i < this.elementSorts.size()-1) 
            {
                result += " ";
            }
        }
        result += ")";
        return result;
    }      

    @Override
    public void accept(SmtAstVisitor visitor) {
        visitor.visit(this);
    }
}
