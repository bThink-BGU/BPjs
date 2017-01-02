/**
 * Some classes, such at {@link BProgram} or {@link BThread}, have to be called 
 * from Javascript. In order to make clear which parts should be exposed to 
 * client Javascript code and which parts are implementation-internal, these
 * objects expose themselves to Javascript code using proxies.
 * 
 * These proxies live in this package.
 * 
 * Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy;
