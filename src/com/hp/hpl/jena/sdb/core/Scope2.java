/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import java.util.*;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;

public class Scope2 implements Scope
{
    Scope left ; 
    Scope right ;
    
    public Scope2(Scope left, Scope right) { this.left = left ; this.right = right ; } 
    
    public boolean hasColumnForVar(Var var)
    { 
        if ( left.hasColumnForVar(var) )
            return true ;
        if ( right.hasColumnForVar(var) )
            return true ;
        return false ;
    }
        
    public Collection<Var> getVars()
    {
        // Better - implement Iterable 
        Set<Var> acc = new LinkedHashSet<Var>() ;
        acc.addAll(left.getVars()) ;
        acc.addAll(right.getVars()) ;
        return acc ;
    }
    
    public SqlColumn getColumnForVar(Var var)
    { 
        SqlColumn c = left.getColumnForVar(var) ;
        if ( c != null )
            return c ;
        c = right.getColumnForVar(var) ;
        if ( c != null )
            return c ;
        return null ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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