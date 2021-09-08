This data structure lets you store data under a name and then search the structure for any object who have names that fully or partially match the search query.

## Example
A TrieMap contains two objects: one named "apple pie", and another named "blueberry pie". By default, these names will be split into keywords around whitespace (although this behaviour can be changed). Searching for "b" or "blue" returns only the object named blueberry pie, and searching for "p" """pie" returns both.

## Customisabilty
The method used to process names can be changed by passing two regular expressions. One for characters that should be removed from the name (by default ' is removed) and one to split the name into keywords (by default whitespace is used).
The order that items are returned in can be changed by passing in a comparator. You can change the ordering based on the attributes of the object itself, and also how well the objects name matches the search query.

## Time complexity
Searches and inserts in O(n) time, where n is the amount of characters in your search/new element.

