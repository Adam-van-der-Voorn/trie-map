package com.vandeadam.util;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TrieNode
 * Recursive data structure that stores stops in a format that allows easy searching of them.
 * */

class TrieNode<T> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<Character, TrieNode<T>> childCharacters = new HashMap<>();
    private final Character character;
    private Map<T, ObjectAssocation<T>> associatedObjects = new HashMap<>();
    private TrieNode<T> parent;

    TrieNode(Character character, TrieNode<T> parent) {
        this.character = character;
        this.parent = parent;
    }

    /**
     * @param searchString the sequence of child characters to find
     * @param index the index in the search string of the next character to look for
     * @return the node in the branch that corresponds to the final character in the search string.
     * @throws NoAssociatedObjectsException if no sequence of nodes that match the input string can be found.
     * */
    TrieNode<T> getNode(String searchString, int index) throws NoAssociatedObjectsException {
        Character workingChar = searchString.charAt(index);
        if (childCharacters.containsKey(workingChar)) {
            if (index == searchString.length()-1) {
                return childCharacters.get(workingChar);
            }
            return childCharacters.get(workingChar).getNode(searchString, index+1);
        }
        else throw new NoAssociatedObjectsException();
    }

    void addAssociation(ObjectAssocation<T> association) {
        associatedObjects.put(association.obj, association);
    }
    void removeAssociation(T obj) {
        associatedObjects.remove(obj);
    }

    TrieNode<T> getChild(Character c) {
        return childCharacters.get(c);
    }

    Collection<TrieNode<T>> getChildren() {
        return Collections.unmodifiableCollection(childCharacters.values());
    }

    /**
     * Recursive function
     * */
    Set<ObjectAssocation<T>> getChildAssociations(Set<ObjectAssocation<T>> childAssociations) {
        if (!associatedObjects.isEmpty()) {
            childAssociations.addAll(associatedObjects.values());
        }
        for (TrieNode<T> child : childCharacters.values()) {
            child.getChildAssociations(childAssociations);
        }
        return childAssociations;
    }

    Collection<ObjectAssocation<T>> getAssociatedObjects() {
        return Collections.unmodifiableCollection(associatedObjects.values());
    }

    /** Recursive function used to insert a new name into the trie.
     * > if the name is one character long, the node is marked as the end of a word and the function is terminated.
     * > otherwise:
     * > a child node is identified that's character is the same as the first character in the input string.
     * > if this node cannot be found, a new child node is created and given the first character of the input string.
     * > the fist character is omitted from the input string and passed to the child node, where the process starts again.
     * */
    void pass(String word, TrieNode.ObjectAssocation<T> associated, int index) {
        Character nextChar = word.charAt(index);
        TrieNode<T> nextNode;

        // if the next character is not a child of this node
        if (!childCharacters.containsKey(nextChar)) {
            nextNode = new TrieNode<>(nextChar, this);
            childCharacters.put(nextChar, nextNode);
        }
        else {
            nextNode = childCharacters.get(nextChar);
        }
        assert nextNode != null;
        // if the next character is the final character
        if (index == word.length()-1) {
            nextNode.addAssociation(associated);
        }
        else {
            nextNode.pass(word, associated, index+1);
        }
    }

    Collection<T> collect() {
    	Collection<T> collection = new HashSet<>();
        for (ObjectAssocation<T> objectAssociation : associatedObjects.values()) {
            collection.add(objectAssociation.obj);
        }
        for (TrieNode<T> child : childCharacters.values()) {
            collection.addAll(child.collect());
        }
        return collection;
    }

    TrieNode<T> getUniqueBranch(String keyword, int index, TrieNode<T> branchBase) throws NoAssociatedObjectsException {
        Character workingChar = keyword.charAt(index);
        if (childCharacters.containsKey(workingChar)) {
            if (index == keyword.length()-1) {
                return branchBase;
            }
            TrieNode<T> child = childCharacters.get(workingChar);
            return child.getUniqueBranch(keyword, index+1, branchBase);
        }
        else throw new NoAssociatedObjectsException();
    }

    TrieNode<T> getParent() {
        return parent;
    }
    Character getChar() {
        return character;
    }
    /**
     * @return a string representing this node and all its children
     */
    public String toString() {
        String result;
        if (childCharacters.isEmpty()) {
            result = character.toString();
        } else if (childCharacters.size() == 1) {
            TrieNode<T> onlyChild = childCharacters.values().iterator().next();
            result = character.toString() + onlyChild.toString();
        } else {
            StringBuilder str = new StringBuilder();
            str.append(character);
            str.append("{");
            // sort children
            List<TrieNode<T>> children = new ArrayList<>(childCharacters.values());
            children.sort((a, b) -> a.getChar() - b.getChar());
            for (int i = 0; i < children.size(); i++) {
                str.append(children.get(i).toString());
                if (i < children.size() - 1) {
                    str.append(",");
                }
            }
            str.append("}");
            result = str.toString();
        }

        return result;
    }

    public void removeChild(char c) {
        childCharacters.remove(c);
    }

    static class ObjectAssocation<T> implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		T obj;
        int keywordIndex;
        int nOfKeywords;
        ObjectAssocation(T obj, int keywordIndex, int nOfKeywords) {
            this.obj = obj;
            this.keywordIndex = keywordIndex;
            this.nOfKeywords = nOfKeywords;
        }
    }
}

