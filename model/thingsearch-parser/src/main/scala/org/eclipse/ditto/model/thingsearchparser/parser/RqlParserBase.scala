/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */
package org.eclipse.ditto.model.thingsearchparser.parser

import org.parboiled2._
import shapeless.HNil

/**
  * RQL Parser base containing commonly used types for both Predicate and Options parsing in EBNF:
  * <pre>
  * Literal                    = FloatLiteral | IntegerLiteral | StringLiteral | "true" | "false" | "null"
  * DoubleLiteral              = [ '+' | '-' ], "0.", Digit, { Digit } | [ '+' | '-' ], DigitWithoutZero, { Digit }, '.', Digit, { Digit }
  * LongLiteral                = '0' | [ '+' | '-' ], DigitWithoutZero, { Digit }
  * DigitWithoutZero           = '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
  * Digit                      = '0' | DigitWithoutZero
  * StringLiteral              = '"', ? printable characters ?, '"'
  * PropertyLiteral            = ? printable characters ?
  * </pre>
  */
protected class RqlParserBase(val input: ParserInput) extends Parser with StringBuilding {

  private val WhiteSpaceChar = CharPredicate(" \n\r\t\f")
  private val CommaClosingParanQuote = CharPredicate(",)\"\\")
  private val CommaClosingParanQuoteSlash = CommaClosingParanQuote ++ "/"
  private val QuoteBackslash = CharPredicate("\"\\")
  private val QuoteSlashBackSlash = QuoteBackslash ++ "/"

  protected def Literal: Rule1[java.lang.Object] = rule {
    (DoubleLiteral | LongLiteral | StringLiteral | "true" ~ WhiteSpace ~ push(java.lang.Boolean.TRUE) |
      "false" ~ WhiteSpace ~ push(java.lang.Boolean.FALSE) | "null" ~ WhiteSpace ~ push(None)) ~ WhiteSpace
  }

  protected def DoubleLiteral: Rule1[java.lang.Double] = rule {
    capture(Integer ~ Frac) ~> (numb => java.lang.Double.valueOf(numb)) ~ WhiteSpace
  }
  protected def LongLiteral: Rule1[java.lang.Long] = rule {
    !"-0" ~ capture(Integer) ~> (numb => java.lang.Long.valueOf(numb)) ~ WhiteSpace
  }

  protected def Integer: Rule[HNil, HNil] = rule {
    optional(anyOf("+-")) ~ (CharPredicate.Digit19 ~ Digits | CharPredicate.Digit)
  }

  protected def Digits: Rule[HNil, HNil] = rule {
    oneOrMore(CharPredicate.Digit)
  }

  protected def Frac: Rule[HNil, HNil] = rule {
    "." ~ Digits
  }

  protected def StringLiteral: Rule1[java.lang.String] = rule {
    '"' ~ clearSB() ~ Characters ~ ws('"') ~ push(sb.toString)
  }

  protected def PropertyLiteral: Rule1[java.lang.String] = rule {
    clearSB() ~ Characters ~ push(sb.toString)
  }

  protected def Characters = rule {
    zeroOrMore(NormalChar | '\\' ~ EscapedChar)
  }

  protected def NormalChar: Rule[HNil, HNil] = rule {
    !CommaClosingParanQuote ~ ANY ~ appendSB()
  }

  protected def EscapedChar: Rule[HNil, HNil] = rule (
    CommaClosingParanQuoteSlash ~ appendSB()
      | 'b' ~ appendSB('\b')
      | 'f' ~ appendSB('\f')
      | 'n' ~ appendSB('\n')
      | 'r' ~ appendSB('\r')
      | 't' ~ appendSB('\t')
      | Unicode ~> { code: Int => sb.append(code.asInstanceOf[Char]); () }
  )

  protected def Unicode: Rule[HNil, shapeless.::[Int, HNil]] = rule {
    'u' ~ capture(CharPredicate.HexDigit ~ CharPredicate.HexDigit ~ CharPredicate.HexDigit ~ CharPredicate.HexDigit) ~>
      ((code: String) => java.lang.Integer.parseInt(code, 16))
  }

  protected def WhiteSpace = rule { zeroOrMore(WhiteSpaceChar) }

  protected def ws(c: Char) = rule { c ~ WhiteSpace }
}
