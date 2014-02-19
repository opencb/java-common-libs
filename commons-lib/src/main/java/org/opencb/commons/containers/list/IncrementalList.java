package org.opencb.commons.containers.list;

import java.util.*;import java.util.LinkedList;import java.util.List;import java.util.ListIterator;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 11/21/13
 * Time: 4:58 PM
 * To change this template use File | Settings | File Templates.
 */
//TODO: Implement List
public class IncrementalList  {

    private List<Long> list;
    private ListIterator<Long> iterator;
    private int totalCount;

    public IncrementalList() {
        list = new LinkedList<>();
        totalCount = 0;
    }

    public void reverseInsert(long i){
        int count = totalCount;
        long element = 0;
        iterator = list.listIterator(list.size());
        while(iterator.hasPrevious()){
            if(count < i){
                break;
            }
            element = iterator.previous();
            count -= element;
        }
        i -= count;
        iterator.add(i);
        if(iterator.hasNext()){
            element = iterator.next() - i;
            iterator.set(element);
        } else {
            totalCount += i;
        }



    }

    public void insert(long i){
        iterator = list.listIterator();
        int count = 0;
        long element;
        while(iterator.hasNext()){
            element = iterator.next();
            count += element;
            if(count > i){
                count -=element;
                iterator.previous();
                break;
            }
        }
        i -= count;
        iterator.add(i);
        if(iterator.hasNext()){
            element = iterator.next() - i;
            iterator.set(element);
        } else {
            totalCount += i;
        }
    }

    public int decrement(){
        long count;
        int numRemoves = 0;
        boolean decremented = false;
        iterator = list.listIterator();

        while(iterator.hasNext() && !decremented){
            count = iterator.next();

            if(count == 0){     //Have to be removed
                iterator.remove();
                numRemoves++;
            } else {
                totalCount--;
                iterator.set(count-1);
                decremented = true;
            }
        }
        return numRemoves;
    }

    public long head(){
        if(!list.isEmpty())
            return list.listIterator().next();
        else
            return -1;
    }
    public int getTotalCount() {
        return totalCount;
    }
}
