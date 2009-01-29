/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
	// MySQL cusors: jdbc:mysql://127.0.0.1/rdswip?useCursorFetch=true&defaultFetchSize=8192&profileSQL=false&enableQueryTimeouts=false&netTimeoutForStreamingResults=0 

    // src-test not in distro.  Does it matter?
    
	// Oracle: Project to NCHAR? See supplied patch.

    // Slot compilation / index form. 
    // Slightly better would be keep constant lookups separate from the SqlNode expression until the
    // unit is compiled.  Currently, can end up with multiple looks of the same thing (but they will be
    // cached in the DB but if not, the query is very expensive anyway and an extra lookup will not
    // add obseravble cost).
    
    // QueryEngineSDB.init ; enable some static optimizations.
    
    // Testing: SDBTestAll does not include the model tests yet because they are not linked to the store description files
    // ParamAllStores for JUnit paramterized tests approach.
    
    // For DISTINCT do in a subquery on the node ids.
    // Similarly, push in the PROJECT to the node ids phase. 
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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
