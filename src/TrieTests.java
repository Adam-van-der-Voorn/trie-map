
import org.junit.Test;

import com.vandeadam.util.NoAssociatedObjectsException;
import com.vandeadam.util.TrieMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TrieTests { //TODO make tests for: getAll(), remove()
    Comparator<TrieMap<String>.SearchResult> fullComparator = (a, b) -> {
        if (a.matchAt(0) && !b.matchAt(0)) return 1;
        if (!a.matchAt(0) && b.matchAt(0)) return -1;
        if (a.matchProportion() > b.matchProportion()) return 1;
        if (a.matchProportion() < b.matchProportion()) return -1;
        int minLength = Math.min(a.getItem().length(), b.getItem().length());
        for (int i = 0; i < minLength; i++) {
            char ac = a.getItem().charAt(i), bc = b.getItem().charAt(i);
            if (ac < bc) return 1;
            if (ac > bc) return -1;
        }
        fail("comparitor should not eval to 0 for this test");
        return 0;
    };
    String[] trieInput = {
            "cheese wheel",
            "edam cheese",
            "sliced edam cheese",
            "sharp cheddar cheese",
            "sliced cheese basic",
            "sliced \"super cow\" cheese",
            "orange cheese sliced",
            "colby aged",
            "apple crumble",
            "apple",
            "apple & pear pie",
            "chicken breast",
            "chicken liver free range",
            "chicken thigh (bone in)",
            "Jimmy's duck breast premuim",
            "whole chicken, frozen",
            "10kg rice bag",
            "knife sharpener"
    };

    /*
    expected.add("<apple crumble>");
    expected.add("<cheese wheel>");
    expected.add("<edam cheese>");
    expected.add("<sliced edam cheese>");
    expected.add("<sharp cheddar cheese>");
    expected.add("<sliced cheese basic>");
    expected.add("<sliced \"super cow\" cheese>");
    expected.add("<orange cheese sliced>");
    expected.add("<colby aged>");
    expected.add("<apple>");
    expected.add("<apple & pear pie>");
    expected.add("<chicken breast>");
    expected.add("<chicken liver free range>");
    expected.add("<chicken thigh (bone in)>");
    expected.add("<Jimmy's duck breast premuim>");
    expected.add("<whole chicken, frozen>");
    expected.add("<10kg rice bag>");
    expected.add("<knife sharpener>")
    */

    String[] trieInput2 = {
            // skipped
            // in front, behind, in the middle, multiple in a row
            // concat
            // in front, behind, in the middle, multiple in a row
            // capitals
            // no caps, some caps, all caps
            "#healthy sugar-free cereal",
            "royal @pple pears",
            "glazed/filled doughnuts",
            "some @*^#! chips",
            "chicken wings (countdown only)",
            "jack's crack",
            "'arr pirate crisps",
            "better than dennis'",
            "can't '''' think of ''' a product",
            "asd chips",
            "Qwerty Calzones",
            "UIOP FROZEN PEAS",
            "Fix & Fogg's \"everything\" butter"
    };
    /*
    expected.add("<#healthy sugar-free cereal>");
    expected.add("<royal @pple pears>");
    expected.add("<glazed/filled doughnuts>");
    expected.add("<some @*^#! chips>");
    expected.add("<chicken wings (countdown only)>");
    expected.add("<jack's crack>");
    expected.add("<'arr pirate crisps>");
    expected.add("<better then dennis'>");
    expected.add("<can't '''' think of ''' a product>");
    expected.add("<asd chips>");
    expected.add("<Qwerty Calzones>");
    expected.add("<UIOP FROZEN PEAS>");
    expected.add("<Fix & Fogg's \"everything\" butter>");*/

    @Test
    public void put_1() {
        TrieMap<String> t = new TrieMap<>();
        t.put("bone broth", "Bb");

        String expected = "root{b{one,roth}}";
        String actual = t.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void put_2() {
        TrieMap<String> t = new TrieMap<>();
        t.put("bear beer bare", "bbb");

        String expected = "root{b{are,e{ar,er}}}";
        String actual = t.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void put_3() {
        TrieMap<String> t = new TrieMap<>();
        for (String name : trieInput) {
            try {
                t.put(name, "<" + name + ">");
            }
            catch (Exception e) {
                fail(name + "\n" + e.toString());
            }
        }
        String expected = "root{" +
                "&," +
                "10kg," +
                "a{ged,pple}," +
                "b{a{g,sic},one,reast}," +
                "c{h{e{ddar,ese},icken},o{lby,w},rumble}," +
                "duck," +
                "edam," +
                "fr{ee,ozen}," +
                "in," +
                "jimmys," +
                "knife," +
                "liver," +
                "orange," +
                "p{ear,ie,remuim}," +
                "r{ange,ice}," +
                "s{harpener,liced,uper}," +
                "thigh," +
                "wh{eel,ole}}";

        String actual = t.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void put_4() {
        TrieMap<String> t = new TrieMap<>();
        for (String name : trieInput2) {
            try {
                t.put(name, "<" + name + ">");
            }
            catch (Exception e) {
                fail(name + "\n" + e.toString());
            }
        }
        String expected = "root{" +
                "&," +
                "a{rr,sd}," +
                "b{etter,utter}," +
                "c{a{lzones,nt},ereal,hi{cken,ps},ountdown,r{ack,isps}}," +
                "d{ennis,oughnuts}," +
                "everything," +
                "f{i{lled,x},oggs,r{ee,ozen}}," +
                "glazed," +
                "healthy," +
                "jacks," +
                "o{f,nly}," +
                "p{ea{rs,s},irate,ple,roduct}," +
                "qwerty," +
                "royal," +
                "s{ome,ugar}," +
                "th{an,ink}," +
                "uiop," +
                "wings}";
        String actual = t.toString();

        assertEquals(expected, actual);
    }
    
    @Test
    public void put_4_emojis() {
        TrieMap<String> t = new TrieMap<>();
        t.put("alien: \uD83D\uDC7D", "<alien emoji + text>");
        String expected = "root{alien}";
        String actual = t.toString();

        assertEquals(expected, actual);
    }
    
    @Test
    public void put_5_emojis() {
        TrieMap<String> t = new TrieMap<>();
        t.put("\uD83D\uDC7D", "<just alien emoji>");
        String expected = "root{}";
        String actual = t.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void search_0_pie() {
        TrieMap<String> t = new TrieMap<>();
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        String search = "pie";

        List<String> expected = new ArrayList<>();
        expected.add("<apple & pear pie>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }


    @Test
    public void search_1_apple_crumble() {
        TrieMap<String> t = new TrieMap<>();
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        String search = "apple crumble";

        List<String> expected = new ArrayList<>();
        expected.add("<apple crumble>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }

    @Test
    public void search_2_edam() {
        TrieMap<String> t = new TrieMap<>((a, b) -> {
            if (a.matchAt(0) && !b.matchAt(0)) return 1;
            if (!a.matchAt(0) && b.matchAt(0)) return -1;
            return 0;
        });
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        String search = "edam";

        List<String> expected = new ArrayList<>();
        expected.add("<edam cheese>");
        expected.add("<sliced edam cheese>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }

    @Test
    public void search_3_a() {
        TrieMap<String> t = new TrieMap<>((a, b) -> {
            if (a.matchAt(0) && !b.matchAt(0)) return 1;
            if (!a.matchAt(0) && b.matchAt(0)) return -1;
            if (a.matchProportion() > b.matchProportion()) return 1;
            if (a.matchProportion() < b.matchProportion()) return -1;
            return 0;
        });
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        String search = "a";

        List<String> expected = new ArrayList<>();
        expected.add("<apple>");
        expected.add("<apple crumble>");
        expected.add("<apple & pear pie>");
        expected.add("<colby aged>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }

    @Test
    public void search_3_c() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        String search = "c";
        List<String> expected = new ArrayList<>();
        expected.add("<cheese wheel>");
        expected.add("<chicken breast>");
        expected.add("<colby aged>");

        expected.add("<chicken liver free range>");
        expected.add("<chicken thigh (bone in)>");

        expected.add("<apple crumble>");
        expected.add("<edam cheese>");

        expected.add("<orange cheese sliced>");
        expected.add("<sharp cheddar cheese>");
        expected.add("<sliced cheese basic>");
        expected.add("<sliced edam cheese>");
        expected.add("<whole chicken, frozen>");

        expected.add("<sliced \"super cow\" cheese>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }

    @Test
    public void search_4_chi() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        String search = "chi";
        List<String> expected = new ArrayList<>();
        expected.add("<chicken breast>");
        expected.add("<chicken liver free range>");
        expected.add("<chicken thigh (bone in)>");
        expected.add("<whole chicken, frozen>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }

    @Test
    public void search_5_chick() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        String search = "chicken whole";
        List<String> expected = new ArrayList<>();
        expected.add("<whole chicken, frozen>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }

    @Test
    public void search_6_che() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        String search = "che";
        List<String> expected = new ArrayList<>();
        expected.add("<cheese wheel>");
        expected.add("<edam cheese>");
        expected.add("<orange cheese sliced>");
        expected.add("<sharp cheddar cheese>");
        expected.add("<sliced cheese basic>");
        expected.add("<sliced edam cheese>");
        expected.add("<sliced \"super cow\" cheese>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }
    @Test
    public void search_6_cheese_sl() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        String search = "cheese sl";
        List<String> expected = new ArrayList<>();
        expected.add("<orange cheese sliced>");
        expected.add("<sliced cheese basic>");
        expected.add("<sliced edam cheese>");
        expected.add("<sliced \"super cow\" cheese>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }

    @Test
    public void search_6_shar() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        String search = "shar";
        List<String> expected = new ArrayList<>();
        expected.add("<sharp cheddar cheese>");
        expected.add("<knife sharpener>");


        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }

    @Test
    public void search_2_1_pple() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }
        String search = "@pple";
        List<String> expected = new ArrayList<>();
        expected.add("<royal @pple pears>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }

    @Test
    public void search_2_2_some_chips() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }
        String search = "some #^%* chips";
        List<String> expected = new ArrayList<>();
        expected.add("<some @*^#! chips>");

        List<String> actual = t.search(search);

        assertEquals(expected, actual);
    }

    @Test
    public void search_2_3_glazed_or_filled() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }
        List<String> expected = new ArrayList<>();
        expected.add("<glazed/filled doughnuts>");

        List<String> actual = t.search("glazed");
        assertEquals(expected, actual);

        actual = t.search("filled");
        assertEquals(expected, actual);
    }

    @Test
    public void search_2_4_jacks_crack() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();
        expected.add("<jack's crack>");

        List<String> actual = t.search("jacks crack");
        assertEquals(expected, actual);

        actual = t.search("jack's crack");
        assertEquals(expected, actual);
    }

    @Test
    public void search_2_5_pirate() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();
        expected.add("<'arr pirate crisps>");

        List<String> actual = t.search("arr pirate");
        assertEquals(expected, actual);

        actual = t.search("'arr pirate'");
        assertEquals(expected, actual);
    }

    @Test
    public void search_2_6_dennis() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();
        expected.add("<better than dennis'>");

        List<String> actual = t.search("dennis");
        assertEquals(expected, actual);

        actual = t.search("dennis'");
        assertEquals(expected, actual);
    }

    @Test
    public void search_2_7_multi_apostrophe() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();
        expected.add("<can't '''' think of ''' a product>");

        List<String> actual = t.search("think of a");
        assertEquals(expected, actual);

        actual = t.search("think of '''''''' a");
        assertEquals(expected, actual);
    }

    @Test
    public void search_2_8_Caps_1() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();
        expected.add("<asd chips>");

        List<String> actual = t.search("asd");
        assertEquals(expected, actual);

        actual = t.search("ASD");
        assertEquals(expected, actual);
    }
    @Test
    public void search_2_8_Caps_2() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();
        expected.add("<Qwerty Calzones>");

        List<String> actual = t.search("QWERTY");
        assertEquals(expected, actual);

        actual = t.search("qwerty");
        assertEquals(expected, actual);
    }

    public void search_2_8_Caps_3() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();
        expected.add("<UIOP FROZEN PEAS>");

        List<String> actual = t.search("UIOP");
        assertEquals(expected, actual);

        actual = t.search("uiop");
        assertEquals(expected, actual);
    }

    @Test
    public void search_2_9_fix_and_fog() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();
        expected.add("<Fix & Fogg's \"everything\" butter>");

        List<String> actual = t.search("fix & foggs butter");
        assertEquals(expected, actual);

        actual = t.search("Fix & Fogg's \"everything\" butter");
        assertEquals(expected, actual);
    }
    
    @Test
    public void search_3_1_emoji() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();
        expected.add("<Fix & Fogg's \"everything\" butter>");

        List<String> actual = t.search("foggs \uD83D\uDC7D");
        assertEquals(expected, actual);
    }
    
    @Test
    public void search_3_2_single_emoji() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();

        List<String> actual = t.search("\uD83D\uDC7D");
        assertEquals(expected, actual);
    }
    
    @Test
    public void search_3_3_single_bracket() {
        TrieMap<String> t = new TrieMap<String>(fullComparator);
        for (String str : trieInput2) {
            t.put(str, "<" + str + ">");
        }

        List<String> expected = new ArrayList<>();

        List<String> actual = t.search("(");
        assertEquals(expected, actual);
    }

    @Test
    public void getAll_1() {
        TrieMap<String> t = new TrieMap<>();
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }

        Collection<String> expected = new HashSet<>();
        for (String str : trieInput) {
            expected.add("<" + str + ">");
        }

        Collection<String> actual = t.items();

        assertEquals(expected, actual);
    }
    @Test
    // root of unique branch == node with multiple children
    public void remove_1() {
        TrieMap<String> t = new TrieMap<>();
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        try {
            t.remove("colby aged", "<colby aged>");
        } catch (NoAssociatedObjectsException e) {
            fail();
        }

        String expected = "root{" +
                "&," +
                "10kg," +
                "apple," + // aged removed
                "b{a{g,sic},one,reast}," +
                "c{h{e{ddar,ese},icken},ow,rumble}," + // colby removed
                "duck," +
                "edam," +
                "fr{ee,ozen}," +
                "in," +
                "jimmys," +
                "knife," +
                "liver," +
                "orange," +
                "p{ear,ie,remuim}," +
                "r{ange,ice}," +
                "s{harpener,liced,uper}," +
                "thigh," +
                "wh{eel,ole}}";
        String actual = t.toString();

        assertEquals(expected, actual);
        assertFalse(t.items().contains("<colby aged>"));
    }

    // root of unique branch == root of trie
    @Test
    public void remove_2() {
        TrieMap<String> t = new TrieMap<>();
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        try {
            t.remove("10kg rice bag", "<10kg rice bag>");
        } catch (NoAssociatedObjectsException e) {
            fail();
        }

        String expected = "root{" +
                "&," +
                // removed 10kg
                "a{ged,pple}," +
                "b{asic,one,reast}," + // removed bag
                "c{h{e{ddar,ese},icken},o{lby,w},rumble}," +
                "duck," +
                "edam," +
                "fr{ee,ozen}," +
                "in," +
                "jimmys," +
                "knife," +
                "liver," +
                "orange," +
                "p{ear,ie,remuim}," +
                "range," + // removed rice
                "s{harpener,liced,uper}," +
                "thigh," +
                "wh{eel,ole}}";
        String actual = t.toString();

        assertEquals(expected, actual);
        assertFalse(t.items().contains("<10kg rice bag>"));
    }

    // root of unique branch == node with single child
    @Test
    public void remove_3() {
        TrieMap<String> t = new TrieMap<>();
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        try {
            t.remove("knife sharpener", "<knife sharpener>");
        } catch (NoAssociatedObjectsException e) {
            fail();
        }

        String expected = "root{" +
                "&," +
                "10kg," +
                "a{ged,pple}," +
                "b{a{g,sic},one,reast}," +
                "c{h{e{ddar,ese},icken},o{lby,w},rumble}," +
                "duck," +
                "edam," +
                "fr{ee,ozen}," +
                "in," +
                "jimmys," +
                // removed knife
                "liver," +
                "orange," +
                "p{ear,ie,remuim}," +
                "r{ange,ice}," +
                "s{harp,liced,uper}," + // removed sharpener
                "thigh," +
                "wh{eel,ole}}";
        String actual = t.toString();

        assertEquals(expected, actual);
        assertFalse(t.items().contains("<knife sharpener>"));
    }

    // multiple obj associations on the obj to remove
    @Test
    public void remove_4() {
        TrieMap<String> t = new TrieMap<>();
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        try {
            t.remove("chicken breast", "<chicken breast>");
        } catch (NoAssociatedObjectsException e) {
            fail();
        }

        String expected = "root{" + // no change to this, because duck breast & chicken thigh exist
                "&," +
                "10kg," +
                "a{ged,pple}," +
                "b{a{g,sic},one,reast}," +
                "c{h{e{ddar,ese},icken},o{lby,w},rumble}," +
                "duck," +
                "edam," +
                "fr{ee,ozen}," +
                "in," +
                "jimmys," +
                "knife," +
                "liver," +
                "orange," +
                "p{ear,ie,remuim}," +
                "r{ange,ice}," +
                "s{harpener,liced,uper}," +
                "thigh," +
                "wh{eel,ole}}";
        String actual = t.toString();

        assertEquals(expected, actual);
        assertFalse(t.items().contains("<chicken breast>"));
    }

    // removing the shorter word in a shared, single child, chain
    // i.e removing "sharp" when "sharpener" also exists
    @Test
    public void remove_5() {
        TrieMap<String> t = new TrieMap<>();
        for (String str : trieInput) {
            t.put(str, "<" + str + ">");
        }
        try {
            t.remove("sharp cheddar cheese", "<sharp cheddar cheese>");
        } catch (NoAssociatedObjectsException e) {
            fail();
        }

        String expected = "root{" +
                "&," +
                "10kg," +
                "a{ged,pple}," +
                "b{a{g,sic},one,reast}," +
                "c{h{eese,icken},o{lby,w},rumble}," + // removed "cheddar", not "cheese" as other items use it
                "duck," +
                "edam," +
                "fr{ee,ozen}," +
                "in," +
                "jimmys," +
                "knife," +
                "liver," +
                "orange," +
                "p{ear,ie,remuim}," +
                "r{ange,ice}," +
                "s{harpener,liced,uper}," + // "sharp" not removed as it is used for sharpener
                "thigh," +
                "wh{eel,ole}}";
        String actual = t.toString();

        assertEquals(expected, actual);
        assertFalse(t.items().contains("<sharp cheddar cheese>"));
    }
}

// things the the user should be able to sort by:

// > whether the matched token position in the object correlates with the token in the search
// i.e. if the search is "sliced cheese" then "sliced cheese" would have a token match for <0> and <1>,
// "sliced edam cheese" would have a token match for <0> and "orange cheese sliced" would have a token match for <2>"

// > the proportion of token matches
// i.e. if the search is "chicken" then "chicken breast" would have 50% token matches, but "chicken thigh bone in"
// would have only 25%

// nothing else of note matters for sorting, just those two



