package com.vandeadam.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 
 * A data structure designed to retrieve a list of stored items based on a search done by a user.
 * 
 * Items are stored alongside a name. This name is broken down into keywords.
 * by default the name is broken down by:
 *  > concatenating the string around , or .
 *  > seperating keywords by using the regex [^a-zA-Z0-9&]+ as the delimiter.
 * eg. the name "bill's $1,000,000 idea (99% successful)" will be split into the keywords: 
 * bills, 1000000, idea, 99, and successful.
 * However these defaults can be changed. 
 * 
 * Items are accessed by searching for thier name.
 * A search will return a List of items that have names with keywords matching any part of the search.
 * If you search for the exact name of the item you want, it is guarnteed to be in the list.
 * However if your search only partially matches the item you want,
 * whether it will be in the list all depends on how you split up your keywords.
 * 
 * This List can be ordered by a given comparator. As well as the properties of the items themselves, 
 * the list can also be ordered by:
 *  > the propotion of keyword matches for that object.
 *  > the order and amount of matches that line up in the search and the name
 *  
 * @param <T> the type of item to store in the trie.
 * */
public class TrieMap<T> {
    private Comparator<SearchResult> comparator = (a, b) -> 0; // compartor for sorting results
    
    /* regexs to aid in seperaing keywords from names  */
    private Pattern toConcat = Pattern.compile("'|,"); // strings that fufull this pattern are removed, strings on either side are concatenated
    private Pattern toSkip = Pattern.compile("[^a-zA-Z0-9&]+"); // pattern used as the delimiter to seperate keywords
    
    private TrieNode<T> rootNode = new TrieNode<>(null, null);
    
    /**
     * Default constructor.
     * Results are not sorted.
     * Default concat pattern = "'|,"
     * Default delimiter = "[^a-zA-Z0-9&]+"
     * */
    public TrieMap() {}

    /**
     * Constructor that takes a comparator for sorting the results.
     * Default concat pattern = "'|,"
     * Default delimiter = "[^a-zA-Z0-9&]+"
     * 
     * @param comparator the comparator to use for sorting the search results. 
     */
    public TrieMap(Comparator<SearchResult> comparator) {
        this.comparator = comparator;
    }

    /**
     * Constructor that changes the default patterns for breaking down the search and item name into keywords.
     * Results are not sorted.
     * 
     * @param toConcat strings that fufull this pattern are removed, strings on either side are concatenated 
     * @param toSkip pattern used as the delimiter to seperate keywords
     */
    public TrieMap(Pattern toConcat, Pattern toSkip) {
        this.toConcat = toConcat;
        this.toSkip = toSkip;
    }

    /**
     * Constructor that changes the default patterns for breaking down the search and item name into keywords, 
     * and also takes a comparator for sorting the results.
     * @param comparator the comparator to use for sorting the search results. 
     * @param toConcat strings that fufull this pattern are removed, strings on either side are concatenated 
     * @param toSkip pattern used as the delimiter to seperate keywords
     */
    public TrieMap(Comparator<SearchResult> comparator, Pattern toConcat, Pattern toSkip) {
        this.comparator = comparator;
        this.toConcat = toConcat;
        this.toSkip = toSkip;
    }

    /**
     * Searches the trie for any items that match or partially match the given name.
     * A item partially matching is where at least one search keyword matches a substring starting at index 0 of at least one keyword associated with the item.
     * This means that if you search for the exact name of the item you want, it is guaranteed to be in the returned list.
     * However if your search only partially matches the item you want,
     * whether it will be in the list all depends on how you split up your keywords.
     *
     * @param name the name of the item to search for.
     * @return a list of all the items associated with the input name, ordered by the comparator in this trie object. 
     * an empty search input returns an empty list.
     * */
    public List<T> search(String name) {
        List<String> keywords = processName(name);
        if (keywords.isEmpty()) {
        	return new ArrayList<>();
        }
        try {
            Map<T, SearchResult> resultsA = searchForKeyword(keywords,0);
            for (int i = 1; i < keywords.size(); i++) {
                Map<T, SearchResult> resultsB = searchForKeyword(keywords, i);
                
                resultsA = resultsA.entrySet().stream()
                		// filter out any items that don't at least partially match both keywords
                        .filter((entry) -> resultsB.containsKey(entry.getKey()))
                        
                        // combine each filtered result in A with the same result in B
                        .map((entry) -> entry.getValue().combine(resultsB.get(entry.getKey())))
                        .collect(Collectors.toMap(SearchResult::getItem, Function.identity()));
            }
            List<SearchResult> results = new ArrayList<>(resultsA.values());
            Collections.sort(results, comparator.reversed());
            return results.stream()
                    .map((e) -> e.item)
                    .collect(Collectors.toList());        
        } 
        // no matches found
        catch (NoAssociatedObjectsException e) { 
            return new ArrayList<>();
        }
    }

    /**
     * puts an item in the trie with the given name.
     * @param name the name of the object.
     * @param item the item that is associated with the given name.
     * */
    public void put(String name, T item) {
        List<String> keywords = processName(name);
        for (int i = 0; i < keywords.size(); i++) {
            rootNode.pass(keywords.get(i), new TrieNode.ObjectAssocation<>(item, i, keywords.size()), 0);
        }
    }

    /**
     * removes an item from the trie.
     * @param name the name of the object.
     * @param item the item that is associated with the given name.
     * @throws NoAssociatedObjectsException 
     * */
    public void remove(String name, T item) throws NoAssociatedObjectsException {
        List<String> keywords = processName(name);
        for (String keyword : keywords) {
            removeKeyword(item, keyword);
        }
    }

    /**
     * returns all the items in this TrieMap.
     * O(n) compexity, where n is the # of items.
     * @return all of the items in this TrieMap
     */
    public Collection<T> items() {
        return rootNode.collect();
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("root{");

        // sort children for predictability
        List<TrieNode<T>> children = new ArrayList<>(rootNode.getChildren());
        children.sort((a, b) -> a.getChar() - b.getChar());

        for (int i = 0; i < children.size(); i++) {
            str.append(children.get(i).toString());
            if (i < children.size()-1) {
                str.append(",");
            }
        }
        str.append("}");
        return str.toString();
    }

    private List<String> processName(String name) {
        name = toConcat.matcher(name).replaceAll("");
        List<String> keywords = new ArrayList<>();
        Scanner in = new Scanner(name);
        in.useDelimiter(toSkip);
        while (in.hasNext()) {
            keywords.add(in.next().toLowerCase());
        }
        in.close();
        return keywords;
    }

    private Map<T, SearchResult> searchForKeyword(List<String> keywords, int keywordIndex) throws NoAssociatedObjectsException{
        
    	TrieNode<T> topNode = rootNode.getNode(keywords.get(keywordIndex), 0);
        Set<TrieNode.ObjectAssocation<T>> associations = topNode.getChildAssociations(new HashSet<>());
        return associations.stream()
                .map((e) -> new SearchResult(e.obj, keywordIndex, e.keywordIndex, keywords.size(), e.nOfKeywords))
                .collect(Collectors.toMap(SearchResult::getItem, Function.identity(), (existing, replacement) -> existing));
    }

    private void removeKeyword(T associated, String keyword) throws NoAssociatedObjectsException {
        TrieNode<T> node = rootNode.getNode(keyword, 0);

        // case where the keyword to remove is not a leaf node,
        // or where the keyword to remove is a leaf node with multiple associations
        if (!node.getChildren().isEmpty() || node.getAssociatedObjects().size() > 1) {
            node.removeAssociation(associated);
            return;
        }

        TrieNode<T> parent = node.getParent();
        int i = keyword.length()-1;
        while (parent.getChildren().size() == 1 && parent.getAssociatedObjects().size() == 0) {
            node = parent;
            parent = node.getParent();
            i--;
        }
        parent.removeChild(keyword.charAt(i));
    }
    
    /**
     * @author ajvdv
     *
     */
    public class SearchResult {
        private final T item;
        private final int nOfObjectKeywords;
        private int nOfMatches = 0;
        private boolean[] matchTable;
        private float matchProportion = 0.0f;

        SearchResult(T item, int searchIndex, int storedIndex, int nOfSearchKeywords, int nOfObjKeywords) {
            this.item = item;
            this.nOfObjectKeywords = nOfObjKeywords;
            matchTable = new boolean[nOfSearchKeywords];
            if (searchIndex == storedIndex) {
                matchTable[searchIndex] = true;
            }
            newMatch();
        }

        SearchResult combine(SearchResult other) {
            assert (item.equals(other.item)) : "cannot intersect with a result with different obj association";
            newMatch();
            for (int i = 0; i < matchTable.length; i++) {
                if (other.matchAt(i)) {
                    matchTable[i] = true;
                }
            }
            return this;
        }

        /**
         * @param n
         * @return whether the nth keyword in the seach is also the nth keyword in this objects name
         */
        public boolean matchAt(int n) {
            return matchTable[n];
        }

        /**
         * @return the proportion of keyword matches in this result
         */
        public float matchProportion() {
            return matchProportion;
        }

        /**
         * @return the item in this search result 
         */
        public T getItem() {
            return item;
        }

        public boolean equals(Object other) {
            if (other.getClass().toString().equals(getClass().toString())) {
                @SuppressWarnings("unchecked")
				SearchResult otherSR = (SearchResult) other;
                return otherSR.item.equals(item);
            }
            return false;
        }

        public int hashCode() {
            return item.hashCode();
        }

        private void newMatch() {
            nOfMatches++;
            matchProportion = ((float)nOfMatches) / nOfObjectKeywords;
        }
    }

}
