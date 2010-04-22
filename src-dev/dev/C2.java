/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator ;

import org.openjena.atlas.lib.Pair ;


public class C2
{
    // interface or class
    // record? or present the structure?
    // structure.
    
    public interface Value {} // ByteBuffer

    public class Key<T> {}  // ByteBuffer //extends Tuple<KeyElt>{}
    
    
    public interface KeyPattern<K> {} // extends Tuple<KeyElt> {}
    public interface SortOrder {}
    

    
    
    
    public interface KeyValueStore<K,V>
    {
        V get(K key) ;

        void put(K key, V value) ;

        void delete(K key) ;

        void close() ;
    }

    //--- The tuple indexes  

    public interface KeyValueStreamStore<K,V> extends KeyValueStore<K,V>
    {
        Iterator<V> findValues(KeyPattern<K> key, SortOrder sortOrder) ;
        Iterator<Pair<K,V>> findPairs(KeyPattern<K> key, SortOrder sortOrder) ;

        void add(Iterator<Pair<K, V>> stuff) ;
        void add2(Pair<K, V>[] stuff) ;
    }

}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */