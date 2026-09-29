/*
 * The MIT License
 *
 * Copyright 2026 michael.
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
package il.ac.bgu.cs.bp.bpjs.bprogramio;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Rhino 1.9.1 compiles regex literals into objects that are not serializable,
 * which broke snapshot cloning during DFS traversal. These tests force snapshot
 * cloning while regular expressions are reachable from b-thread state.
 */
public class RegExpSerializationTest {

    /**
     * A b-thread holds a reference to a function containing a regex literal
     * with a character class, while other b-threads branch.
     */
    private static final String CLOSED_OVER_FUNCTION_PROGRAM = String.join("\n",
        "const next = bp.Event('next');",
        "const DIRECTIONS = bp.EventSet('directions', e => e.name == 'direction');",
        "function checkRows(text){ return parseFloat(text.replace(%s, '')); }",
        "function checkColumns(text){ return parseFloat(text.replace(%s, '')); }",
        "bp.registerBThread('chooser', function(){",
        "    bp.sync({request:[bp.Event('direction', 'rows'), bp.Event('direction', 'columns')]});",
        "    bp.sync({request:[bp.Event('extra check'), bp.Event('no extra check')]});",
        "    bp.sync({request:next});",
        "});",
        "bp.registerBThread('Perform check', function(){",
        "    let direction = bp.sync({waitFor:DIRECTIONS}).data;",
        "    let checker = (direction == 'rows') ? checkRows : checkColumns;",
        "    bp.sync({request:bp.Event('checked ' + direction + ': ' + checker('$1,234.5')), block:next});",
        "});"
    );

    @Test
    public void regexLiteralInClosedOverFunction() throws Exception {
        VerificationResult res = verify(String.format(CLOSED_OVER_FUNCTION_PROGRAM,
                "/[^0-9.-]/g", "/[^0-9.-]/g"));
        assertFalse(res.isViolationFound());
    }

    @Test
    public void liveRegExpSurvivesCloning() throws Exception {
        VerificationResult res = verify(String.join("\n",
            "bp.registerBThread('holder', function(){",
            "    const re = /[a-z]+/g;",
            "    bp.ASSERT(re.test('abc def'), 'first match failed');",
            "    bp.ASSERT(re.lastIndex === 3, 'bad lastIndex before sync: ' + re.lastIndex);",
            "    bp.sync({request:[bp.Event('branch'), bp.Event('no branch')]});",
            "    bp.ASSERT(String(re) === '/[a-z]+/g', 'regex changed: ' + String(re));",
            "    bp.ASSERT(re.lastIndex === 3, 'lastIndex not preserved: ' + re.lastIndex);",
            "    bp.ASSERT(re.test('abc def'), 'second match failed');",
            "    bp.ASSERT(re.lastIndex === 7, 'bad lastIndex after second match: ' + re.lastIndex);",
            "    bp.sync({request:bp.Event('done')});",
            "});"
        ));
        assertFalse(res.isViolationFound() ? res.getViolation().get().describe() : "", res.isViolationFound());
    }

    @Test
    public void regexDoesNotInflateStateSpace() throws Exception {
        VerificationResult withRegex = verify(
                String.format(CLOSED_OVER_FUNCTION_PROGRAM, "/[^0-9.-]/g", "/[^0-9.-]/g"));
        VerificationResult withoutRegex = verify(
                String.format(CLOSED_OVER_FUNCTION_PROGRAM, "'$'", "','"));

        assertEquals(withoutRegex.getScannedStatesCount(), withRegex.getScannedStatesCount());
        assertEquals(withoutRegex.getScannedEdgesCount(), withRegex.getScannedEdgesCount());
    }

    private static VerificationResult verify(String code) throws Exception {
        StringBProgram bprog = new StringBProgram(code);
        return new DfsBProgramVerifier(bprog).verify(bprog);
    }
}
