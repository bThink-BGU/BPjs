/**
 * Some classes, such at {@link BProgram} or {@link BThread}, have to be called 
 * from Javascript. In order to have a clear separation between 
 * client Javascript code and implementation-internal code, these
 * objects expose themselves to Javascript code using proxies.
 * 
 * These proxies live in this package.
 * 
 * Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy;
