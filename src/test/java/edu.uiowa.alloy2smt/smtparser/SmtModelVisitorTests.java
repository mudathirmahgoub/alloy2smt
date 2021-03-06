package edu.uiowa.alloy2smt.smtparser;

import edu.uiowa.alloy2smt.smtAst.SmtModel;
import edu.uiowa.alloy2smt.smtparser.antlr.SmtLexer;
import edu.uiowa.alloy2smt.smtparser.antlr.SmtParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SmtModelVisitorTests
{

    SmtModel parseModel(String model)
    {
        CharStream charStream = CharStreams.fromString(model);

        SmtLexer lexer = new SmtLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SmtParser parser = new SmtParser(tokenStream);

        ParseTree tree =  parser.model();
        SmtModelVisitor visitor = new SmtModelVisitor();
        SmtModel smtModel = (SmtModel) visitor.visit(tree);
        return  smtModel;
    }

    @Test
    void model1()
    {
        String model =
                "(model\n" +
                "; cardinality of Atom is 1\n" +
                "(declare-sort Atom 0)\n" +
                "; rep: @uc_Atom_0\n" +
                "(declare-sort UnaryIntTup 0)\n" +
                "(declare-sort BinaryIntTup 0)\n" +
                "(declare-sort TernaryIntTup 0)\n" +
                "(define-fun value_of_unaryIntTup ((BOUND_VARIABLE_448 UnaryIntTup)) (Tuple Int) (mkTuple 0))\n" +
                "(define-fun value_of_binaryIntTup ((BOUND_VARIABLE_457 BinaryIntTup)) (Tuple Int Int) (mkTuple 0 0))\n" +
                "(define-fun value_of_ternaryIntTup ((BOUND_VARIABLE_466 TernaryIntTup)) (Tuple Int Int Int) (mkTuple 0 0 0))\n" +
                "(define-fun atomNone () (Set (Tuple Atom)) (as emptyset (Set (Tuple Atom))))\n" +
                "(define-fun atomUniv () (Set (Tuple Atom)) (as emptyset (Set (Tuple Atom))))\n" +
                "(define-fun atomIden () (Set (Tuple Atom Atom)) (as emptyset (Set (Tuple Atom Atom))))\n" +
                ")";

        SmtModel smtModel = parseModel(model);
        Assertions.assertEquals(4, smtModel.getSorts().size());
        Assertions.assertEquals(6, smtModel.getFunctionDefinitions().size());
    }
}