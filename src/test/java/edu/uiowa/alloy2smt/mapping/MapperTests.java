package edu.uiowa.alloy2smt.mapping;

import edu.uiowa.alloy2smt.Utils;
import edu.uiowa.alloy2smt.translators.Translation;
import edu.uiowa.alloy2smt.translators.TranslatorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MapperTests
{
    @Test
    void signature1()
    {
        String alloy = "sig A {} \n fact f {#A = 3}";
        Translation translation = Utils.translate(alloy);
        Mapper mapper = translation.getMapper();
        Assertions.assertNotNull(mapper);

        MappingSignature signature = mapper.signatures
                .stream().filter(s -> s.label.equals("this/A"))
                .findFirst().get();


        Assertions.assertFalse(signature.builtIn);
        Assertions.assertFalse(signature.isAbstract);
        Assertions.assertFalse(signature.id < TranslatorUtils.UNIV_SIGNATURE_ID);
        Assertions.assertEquals(TranslatorUtils.UNIV_SIGNATURE_ID, signature.parentId);
        Assertions.assertEquals("this_A", signature.functionName);
    }


    @Test
    void field1()
    {
        String alloy =
                "sig A {f: A, g: A -> A} \n" +
                "sig B {f: A, g: B -> A}" +
                " fact f {#A = 3 and #B = 4}";
        Translation translation = Utils.translate(alloy);
        Mapper mapper = translation.getMapper();
        Assertions.assertNotNull(mapper);

        MappingSignature signatureA = mapper.signatures
                .stream().filter(s -> s.label.equals("this/A"))
                .findFirst().get();

        MappingSignature signatureB = mapper.signatures
                .stream().filter(s -> s.label.equals("this/B"))
                .findFirst().get();

        Assertions.assertEquals(TranslatorUtils.UNIV_SIGNATURE_ID, signatureA.parentId);
        Assertions.assertEquals(TranslatorUtils.UNIV_SIGNATURE_ID, signatureB.parentId);

        Assertions.assertEquals("this_A", signatureA.functionName);
        Assertions.assertEquals("this_B", signatureB.functionName);

        MappingField fieldA_f = mapper.fields.stream()
                .filter(f -> f.parentId == signatureA.id && f.functionName.equals("this_A_f"))
                .findFirst().get();

        MappingField fieldA_g = mapper.fields.stream()
                .filter(f -> f.parentId == signatureA.id && f.functionName.equals("this_A_g"))
                .findFirst().get();

        MappingField fieldB_f = mapper.fields.stream()
                .filter(f -> f.parentId == signatureB.id && f.functionName.equals("this_B_f"))
                .findFirst().get();

        MappingField fieldB_g = mapper.fields.stream()
                .filter(f -> f.parentId == signatureB.id && f.functionName.equals("this_B_g"))
                .findFirst().get();

        Assertions.assertEquals("f",fieldA_f.label);
        Assertions.assertEquals("g",fieldA_g.label);
        Assertions.assertEquals("f",fieldB_f.label);
        Assertions.assertEquals("g",fieldB_g.label);

        Assertions.assertEquals(2, fieldA_f.types.size());
        Assertions.assertEquals(signatureA.id, fieldA_f.types.get(0).id);
        Assertions.assertEquals(signatureA.id, fieldA_f.types.get(1).id);

        Assertions.assertEquals(3    , fieldA_g.types.size());
        Assertions.assertEquals(signatureA.id, fieldA_g.types.get(0).id);
        Assertions.assertEquals(signatureA.id, fieldA_g.types.get(1).id);
        Assertions.assertEquals(signatureA.id, fieldA_g.types.get(2).id);

        Assertions.assertEquals(2    , fieldB_f.types.size());
        Assertions.assertEquals(signatureB.id, fieldB_f.types.get(0).id);
        Assertions.assertEquals(signatureA.id, fieldB_f.types.get(1).id);


        Assertions.assertEquals(3    , fieldB_g.types.size());
        Assertions.assertEquals(signatureB.id, fieldB_g.types.get(0).id);
        Assertions.assertEquals(signatureB.id, fieldB_g.types.get(1).id);
        Assertions.assertEquals(signatureA.id, fieldB_g.types.get(2).id);
    }
}