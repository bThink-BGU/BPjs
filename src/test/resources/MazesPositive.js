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

var trivialPlus = ["s  ",
                   "## ",
                   "t  "];

var simple = ["       t ",
              "##  #####",
              "         ",
              "######## ",
              "s        "];

var complex = ["    #  t ",
               "## ## ###",
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
        [" ______ ",
            "< BPjs >       @@@@@@@@@   t",
            " ------ ",
            "        \   ^__^    @@@@@@@@",
            "      @  \  (oo)\_______",
            "@@@@@@@     (__)\       )\\/\\",
            " s              ||----w |",
            "@@@@@@@         ||     ||"];

var mazes = {
    trivial: trivial,
    trivialPlus: trivialPlus,
    simple: simple,
    cow: cow,
    complex: complex,
    singleSolution: singleSolution
};


function enterEvent(c, r) {
    return bp.Event("Enter (" + c + "," + r + ")");//, {col:c, row:r});
}

var anyEntrance = bp.EventSet("AnyEntrance", function(evt){
   return evt.name.indexOf("Enter") === 0;
});

function adjacentCellEntries(col, row) {
    return [enterEvent(col + 1, row), enterEvent(col - 1, row),
        enterEvent(col, row + 1), enterEvent(col, row - 1)];

}

function evt2coord(evt) {
    if (!evt.name.startsWith("Enter")) {
        return undefined;
    }
    var coordStr = evt.name.split("\\(")[1];
    var coord = coordStr.split(",");
    return {
        col: Number(coord[0]),
        row: Number(coord[1].split("\\)")[0])
    };
}

if (!MAZE_NAME) {
    MAZE_NAME = "trivial";
}

var maze = mazes[MAZE_NAME];

parseMaze(maze);

bp.registerBThread("onlyOnce", function(){
    var block = [];
    while (true) {
        var evt = bp.sync({waitFor: anyEntrance, block: block});
        block.push(evt);
    }
});

////////////////////////
///// functions 
function parseMaze(mazeLines) {
    for (var row = 0; row < mazeLines.length; row++) {
        for (var col = 0; col < mazeLines[row].length; col++) {
            var currentPixel = mazeLines[row].substring(col, col + 1);
            if (currentPixel === " " || currentPixel === "t" || currentPixel === "s") {
                if (currentPixel === "t") {
                    addTargetCell(col, row);
                }
                if (currentPixel === "s") {
                    addWalker(col, row);
                }
            } else {
                addWall(col, row);
            }
        }
    }
    addEnclosingWalls(mazeLines[0].length, mazeLines.length);
}

function addWall(col, row) {
    bp.registerBThread("wall(" + col + "," + row + ")", function(){
        bp.sync({block: enterEvent(col, row)});
    });
}

function addEnclosingWalls(cols, rows) {
    var topWall = bp.EventSet("externalWalls", function (e) {
        var coords = evt2coord(e);
        if (coords) {
            if (coords.row < 0) return true;
            if (coords.col < 0) return true;
            if (coords.row >= rows) return true;
            if (coords.col >= cols) return true;
        }
        return false;
    });
    bp.registerBThread("externalWallsBt", function () {
        bp.sync({block: topWall});
    });
}

/**
 * Waits for an event signaling the entrance to the 
 * target cell, then blocks everything.
 * @param {type} col
 * @param {type} row
 * @returns {undefined}
 */
function addTargetCell(col, row) {
    bp.registerBThread("Target(c:" + col + " r:" + row + ")", function () {
        bp.sync({
            waitFor: enterEvent(col, row)
        });

        bp.sync({
            request: TARGET_FOUND_EVENT,
            block: bp.allExcept(TARGET_FOUND_EVENT)
        });
    });
}

function addWalker(col, row) {
    bp.registerBThread("starter(c:" + col + " r:" + row + ")", function () {
        var curCol = col;
        var curRow = row;
        bp.sync({
            request: enterEvent(col, row)
        });
        while (true) {
            var evt = bp.sync({
                request: adjacentCellEntries(curCol, curRow),
                interrupt: TARGET_FOUND_EVENT
            });
            var coords = evt2coord(evt);
            curCol = coords.col;
            curRow = coords.row;
        }
    });
}