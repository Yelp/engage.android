/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2012, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.janrain.capture;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class JRCapturePlural<T> implements Set<T> {
    private final HashSet<T> hashSet = new HashSet<T>();

    public Object[] toArray() {
        return hashSet.toArray();
    }

    public <T> T[] toArray(T[] ts) {
        return hashSet.toArray(ts);
    }

    public boolean add(T t) {
        return hashSet.add(t);
    }

    public boolean containsAll(Collection<?> objects) {
        return hashSet.containsAll(objects);
    }

    public boolean addAll(Collection<? extends T> ts) {
        return hashSet.addAll(ts);
    }

    public boolean retainAll(Collection<?> objects) {
        return hashSet.retainAll(objects);
    }

    public boolean equals(Object o) {
        return hashSet.equals(o);
    }

    public int hashCode() {
        return hashSet.hashCode();
    }

    public boolean removeAll(Collection<?> objects) {
        return hashSet.removeAll(objects);
    }

    public Iterator<T> iterator() {
        return hashSet.iterator();
    }

    public int size() {
        return hashSet.size();
    }

    public boolean isEmpty() {
        return hashSet.isEmpty();
    }

    public boolean contains(Object o) {
        return hashSet.contains(o);
    }

    public boolean remove(Object o) {
        return hashSet.remove(o);
    }

    public void clear() {
        hashSet.clear();
    }
}