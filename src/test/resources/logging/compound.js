/* 
 * The MIT License
 *
 * Copyright 2020 michael.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/* global bp, java */
importClass(java.util.HashSet);
importClass(java.util.HashMap);
importClass(java.util.ArrayList);

bp.log.info("Simple String");
bp.log.info(1);
bp.log.info(1.5);
bp.log.info(["I'm","an","array"]);
bp.log.info({im:"an", ob:"ject", arr:[1,2,"three"]});

var set = new HashSet();
set.add("a");
set.add("b");
set.add("c");
bp.log.info( set );

var lst = new ArrayList();
lst.add("a");
lst.add("b");
lst.add("c");
bp.log.info( lst );

var map = new HashMap();
map.put("a","A");
map.put("b","B");
map.put("c","C");
bp.log.info( map );