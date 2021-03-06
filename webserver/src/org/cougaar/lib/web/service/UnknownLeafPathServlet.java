/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.lib.web.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A "/$name[/.*]" level servlet that:<ol>
 *   <li>generates a help message for "/$name" and "/$name/" 
 *       requests, and</li>
 *   <li>generates an error message for all other (unknown)
 *       paths</li>
 * </ol>.
 */
public class UnknownLeafPathServlet implements Servlet {

  private final String realName;

  public UnknownLeafPathServlet(String realName) {
    this.realName = realName;

    String s = (realName == null ? "realName" : null);
    if (s != null) {
      throw new IllegalArgumentException("null "+s);
    }
  }

  public void service(
      ServletRequest sreq, ServletResponse sres
      ) throws ServletException, IOException {

    // cast
    HttpServletRequest req = (HttpServletRequest) sreq;
    HttpServletResponse res = (HttpServletResponse) sres;

    // get the "/$name[/.*]"
    String path = req.getRequestURI();

    String name = realName;
    String trimPath = null;

    int pathLength = (path == null ? 0 : path.length());
    if (pathLength > 1) {
      if (pathLength >= 2 &&
          path.charAt(0) == '/' &&
          path.charAt(1) == '$') {
        int j = path.indexOf('/', 2);
        if (j < 0) {
          j = pathLength;
        } else if (j < (pathLength - 1)) {
          trimPath = path.substring(j);
        }
        if (j > 2) {
          name = path.substring(2, j);
        }
      } else {
        trimPath = path;
      }
    }

    boolean is_error = (trimPath != null);

    res.setContentType("text/html");
    if (is_error) {
      // generate an HTML error response, with a 404 error code.
      //
      // use "setStatus" instead of "sendError" -- see bug 1259
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.addHeader("Cougaar-error", "path");
    }

    PrintWriter out = res.getWriter();

    String title =
      (is_error ? "\""+path+"\" Not Found on " : "")+
      "Agent \""+name+"\""+
      (name.equals(realName) ? "" : " ("+realName+")");

    out.print(
        "<html><head><title>"+
        title+
        "</title></head><body>\n"+
        (is_error ? ("<p><h1>"+title+"</h1>\n") : ("<h2>"+title+"</h2>\n"))+
        "<p>Options:<ul>\n"+
        "<li><a href=\"/$"+name+"/list\">"+
        "List <b>paths</b> in agent \""+name+"\""+
        "</a></li><p>\n"+
        "<li><a href=\"/$"+name+"/agents\">"+
        "List <b>local agents</b> co-located with agent \""+name+"\""+
        "</a></li><p>\n"+
        "<li><a href=\"/$"+name+"/agents?suffix=.\">"+
        "List <b>all agents</b> in the society"+
        "</a></li>\n"+
        "</ul>\n"+
        "</body></html>");
    out.close();
  }

  // etc
  private ServletConfig config;
  public void init(ServletConfig config) { this.config = config; }
  public ServletConfig getServletConfig() { return config; }
  public String getServletInfo() { return "unknown-leaf-path"; }
  public void destroy() { }

}
