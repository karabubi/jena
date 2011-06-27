/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.file.FileException ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;

public class ObjectFileTrans implements ObjectFile, Transactional
{
    private final ObjectFile other ;
    private long otherAllocOffset ;           // record where we start allocating
    private boolean passthrough = false ;
    private boolean inTransaction = false ;
    private final ObjectFile base ;
    
    // For recovery replay, we need to truncate "base" first. 
    
    public ObjectFileTrans(Transaction txn, ObjectFile base, ObjectFile other)
    {
        // The other object file must use the same allocation policy.
        this.base = base ;
        this.other = other ;
        inTransaction = false ;

        //  [TxTDB:PATCH-UP] Begin is not being called.
        this.otherAllocOffset = base.length() ;
        //Log.info(this, getLabel()+": otherAllocOffset = "+otherAllocOffset) ;
    }

    // Begin read ==> passthrough.
    
    @Override
    public void begin(Transaction txn)
    {
        passthrough = false ;
        inTransaction = true ;
        other.reposition(0) ;
        this.otherAllocOffset = base.length() ;
    }
    
    @Override
    public void commit(Transaction txn)
    {
        if ( ! inTransaction )
            throw new TDBTransactionException("Not in a transaction for a commit to happen") ; 
        append() ;
        base.sync() ;
        other.reposition(0) ;
        passthrough = true ;
    }

    @Override
    public void abort(Transaction txn)
    {
        other.reposition(0) ;
    }
    
    /** Copy from the temporary file to the real file */
    public /*temporary*/ void append()
    {
        // We could write directly to the real file if:
        //   we record the truncate point needed for an abort
        //   manage partial final writes
        //   deny the existence of nodes after the transaction mark.
        // Later - stay simple for now.
        
        // Truncate/position the ObjectFile.
        base.reposition(otherAllocOffset) ;
        
        Iterator<Pair<Long, ByteBuffer>> iter = other.all() ;
        for ( ; iter.hasNext() ; )
        {
            Pair<Long, ByteBuffer> p = iter.next() ;
            String s = StrUtils.fromUTF8bytes(p.getRight().array()) ;
            
            long x = base.write(p.getRight()) ;
            
            if ( p.getLeft()+otherAllocOffset != x )
                throw new FileException("Expected id of "+(p.getLeft()+otherAllocOffset)+", got an id of "+x) ;
        }
    }
    
    public void setPassthrough(boolean v) { passthrough = v ; }
    
    @Override
    public void reposition(long id)
    {
        if ( passthrough ) { base.reposition(id) ; return ; }
        if ( id > otherAllocOffset )
        {
            other.reposition(mapToOther(id)) ;
            return ;
        }
        
        Log.warn(this, "Unexpected: Attempt to reposition over base file") ;
        base.reposition(id) ;
        other.reposition(0) ;
        otherAllocOffset = base.length() ;
    }

    @Override
    public Block allocWrite(int maxBytes)
    {
        if ( passthrough ) return base.allocWrite(maxBytes) ;
        Block block = other.allocWrite(maxBytes) ;
        block = new Block(block.getId()+otherAllocOffset, block.getByteBuffer()) ;
        return block ;
    }

    @Override
    public void completeWrite(Block block)
    {
        if ( passthrough ) { base.completeWrite(block) ; return ; } 
        block = new Block(block.getId()-otherAllocOffset, block.getByteBuffer()) ;
        other.completeWrite(block) ;
    }

    /** Convert from a id to the id in the "other" file */ 
    private long mapToOther(long x) { return x-otherAllocOffset ; }
    /** Convert from a id in other to an external id  */ 
    private long mapFromOther(long x) { return x+otherAllocOffset ; }
    
    @Override
    public long write(ByteBuffer buffer)
    {
        if ( passthrough ) { return base.write(buffer) ; } 
        // Write to auxillary
        long x = other.write(buffer) ;
        return mapFromOther(x) ;
    }

    @Override
    public ByteBuffer read(long id)
    {
        if ( passthrough ) { return base.read(id) ; } 
        if ( id < otherAllocOffset )
            return base.read(id) ;
        long x = mapToOther(id) ; 
        return other.read(id-otherAllocOffset) ;
    }

    @Override
    public long length()
    {
        if ( passthrough ) { return base.length() ; } 
        return otherAllocOffset+other.length() ;
    }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all()
    {
        if ( passthrough ) { return base.all() ; } 
        return Iter.concat(base.all(), other.all()) ;
    }

    @Override
    public void sync()
    { 
        if ( passthrough ) { base.sync() ; return ; } 
    }

    @Override
    public void close()
    {
        if ( passthrough ) { base.close() ; return ; }
    }

    @Override
    public String getLabel()
    {
        return "("+base.getLabel()+":"+other.getLabel()+")" ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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