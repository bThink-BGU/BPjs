/* 
 * The MIT License
 *
 * Copyright 2017 michael.
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


/* global bp, MAZE_NAME, TARGET_FOUND_EVENT */

var trivial = 
  ["s  ",
   "   ",
   "  t"];

var trivialPlus = 
  ["s  ",
   "## ",
   "t  "];

var simple =
  ["       t ",
   "##  #####",
   "         ",
   "######## ",
   "s        "];

var complex =
  ["    #  t ",
   "## ## ###",
   "         ",
   "### #### ",
   "   s#    "];

var complex1_5 =
  ["   # #    t ",
   "# ## ## ####",
   "         #  ",
   "#### ###### ",
   "    s#      "];

var complex1_7 =
  ["   # #   t ",
   "# ## # ####",
   "           ",
   " ########  ",
   "        #  ",
   "#### ##### ",
   "    s#     "];
   
var complex2 = 
   ["s%          %      ",
    " %%%%%%%% %%%%% %%%",
    "                   ",
    " %%%%%%%%%%%%%% %%%",
    " %           %  %  ",
    " %%%%%%%%%%        ",
    "        %    %%%% %",
    " %%%%% %%%%  % t   "];

var multipleSolutions = 
   ["s                 ",
    "XXXXX YYYY ZZZ ZZZ",
    "  X    Y   Z Z  Z ",
    "  X        Z    Z ",
    "  XXXXX    ZZZZZZ ",
    "                 t"];
var noSolution =
  ["    #  t ",
   "## ######",
   "         ",
   "### #### ",
   "   s#    "];

var singleSolution = 
  ["  *     t",
   "*** *****",
   "  * *    ",
   "*** *****",
   "  *s*    "];
   
var cow = 
        [" _______                      ",
         "< BPjmoo >       @@@@@@@@@   t",
         " -------                      ",
         "        \   ^__^    @@@@@@@@",
         "      @  \  (oo)\_______",
         "@@@@@@@     (__)\       )\\/\\",
         " s              ||----w |",
         "@@@@@@@         ||     ||"];
 
function enterEvent(c,r) {
    return bp.Event("Enter (" + c + ","  + r + ")");//, {col:c, row:r});
}

var anyEntrance = bp.EventSet("AnyEntrance", function(evt){
   return evt.name.indexOf("Enter") === 0;
});

function adjacentCellEntries(col, row) {
    return [enterEvent(col + 1, row), enterEvent(col - 1, row),
        enterEvent(col, row + 1), enterEvent(col, row - 1)];

}

if ( ! MAZE_NAME )  {
    MAZE_NAME = "trivial";
}

var maze = eval(MAZE_NAME);

parseMaze(maze);

bp.registerBThread("onlyOnce", function(){
    var block = [];
    while ( true ) {
        var evt = bp.sync({waitFor:anyEntrance, block:block});
        block.push(evt);
    }
});

////////////////////////
///// functions 
function parseMaze(mazeLines) {
    for ( var row=0; row<mazeLines.length; row++ ) {
        for ( var col=0; col<mazeLines[row].length; col++ ) {
            var currentPixel = mazeLines[row].substring(col,col+1);
            if ( currentPixel===" " || currentPixel==="t" || currentPixel==="s" ) {
                addSpaceCell(col, row);
                if ( currentPixel==="t" ) {
                    addTargetCell(col, row);
                }
                if ( currentPixel==="s" ) {
                    addStartCell(col, row);
                }
            }
        }
    }
}


/**
 * A cell the maze solver can enter. Waits for entrances to one of the cell's
 * neighbours, then requests entrance to itself.
 * @param {type} col
 * @param {type} row
 * @returns {undefined}
 */
function addSpaceCell( col, row ) {
    bp.registerBThread("cell(c:"+col+" r:"+row+")",
        function() {
            while ( true ) {
                bp.sync({waitFor:adjacentCellEntries(col, row)});
                bp.sync({
                    request: enterEvent(col, row),
                    waitFor: anyEntrance
                });
            }
        }
    );
}

/**
 * Waits for an event signaling the entrance to the 
 * target cell, then blocks everything.
 * @param {type} col
 * @param {type} row
 * @returns {undefined}
 */
function addTargetCell(col, row) {
    bp.registerBThread("Target(c:"+col+" r:"+row+")", function(){
       bp.sync({
           waitFor: enterEvent(col, row)
       }); 
       
       bp.sync({
           request: TARGET_FOUND_EVENT,
           block: bp.allExcept( TARGET_FOUND_EVENT )
       });
    });
}

function addStartCell(col, row) {
    bp.registerBThread("starter(c:"+col+" r:"+row+")", function() {
       bp.sync({
          request:enterEvent(col,row) 
       });
    });
}