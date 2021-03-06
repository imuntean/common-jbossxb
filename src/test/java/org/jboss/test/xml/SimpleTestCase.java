/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package org.jboss.test.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jboss.logging.Logger;
import org.jboss.test.xml.book.Book;
import org.jboss.test.xml.book.BookCharacter;
import org.jboss.test.xml.book.BookGenericObjectModelFactory;
import org.jboss.test.xml.book.BookGenericObjectModelProvider;
import org.jboss.test.xml.book.BookObjectFactory;
import org.jboss.test.xml.book.BookObjectProvider;
import org.jboss.xb.binding.AbstractMarshaller;
import org.jboss.xb.binding.DtdMarshaller;
import org.jboss.xb.binding.GenericObjectModelFactory;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Marshaller;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.ObjectModelProvider;
import org.jboss.xb.binding.SimpleTypeBindings;
import org.jboss.xb.binding.TypeBinding;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.XercesXsMarshaller;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author Scott.Stark@jboss.org
 * @version <tt>$Revision: 43309 $</tt>
 */
public class SimpleTestCase
   extends AbstractJBossXBTest
{
   private static final Logger log = Logger.getLogger(SimpleTestCase.class);

   public SimpleTestCase(String name)
   {
      super(name);
   }

   public void testUnmarshalBookDtd() throws Exception
   {
      // create an object model factory
      ObjectModelFactory factory = new BookObjectFactory();
      unmarshalBook("book-dtd.xml", factory);
   }

   /**
    * Test that parser validation can be disabled to parse a non-conforming doc
    * @throws Exception
    */
   public void testParserValidationFeature()
      throws Exception
   {
      SAXParserFactory saxFactory = SAXParserFactory.newInstance();
      saxFactory.setValidating(true);
      saxFactory.setNamespaceAware(true);
      saxFactory.setXIncludeAware(true);
      //
      SAXParser parser = saxFactory.newSAXParser();
      log.debug("Created parser: "+parser
            + ", isNamespaceAware: "+parser.isNamespaceAware()
            + ", isValidating: "+parser.isValidating()
            + ", isXIncludeAware: "+parser.isXIncludeAware()
            );
      XMLReader reader = parser.getXMLReader();
      // Both these features need to be false
      reader.setFeature(Unmarshaller.VALIDATION, false);
      reader.setFeature(Unmarshaller.DYNAMIC_VALIDATION, false);
      reader.setEntityResolver(new BooksEntityResolver());
      assertFalse(parser.isValidating());
      URL xmlUrl = getResource("/xml/book/books2-dtd.xml");
      log.debug("parsing: "+xmlUrl);
      parser.parse(xmlUrl.openStream(), new DefaultHandler()
         {
            public void error(SAXParseException e) throws SAXException
            {
               throw e;
            }
   
            public void fatalError(SAXParseException e) throws SAXException
            {
               throw e;
            }
         }
      );
   }

   /**
    * Test that one can disable validation to parse a doc that does not
    * conform to its dtd
    * @throws Exception
    */
   public void testUnmarshalBooks2Dtd() throws Exception
   {
      // create an object model factory
      String xmlSource = "books2-dtd.xml";
      ObjectModelFactory factory = new BookObjectFactory();
      log.debug("<test-unmarshal-" + xmlSource + '>');

      // get the XML stream
      URL xmlUrl = getResource("/xml/book/" + xmlSource);

      // create unmarshaller
      Unmarshaller unmarshaller = getBookUnmarshaller();
      unmarshaller.setValidation(false);

      // let the object model factory to create an instance of Book and populate it with data from XML
      Book book = (Book)unmarshaller.unmarshal(xmlUrl.openStream(), factory, null);

      checkUnmarshalledBook(book);

      log.debug("</test-unmarshal-" + xmlSource + '>');
   }

   public void testUnmarshalBookXs() throws Exception
   {
      // create an object model factory
      ObjectModelFactory factory = new BookObjectFactory();
      unmarshalBook("book-xs.xml", factory);
   }

   public void testUnmarshalBookXsGenericFactory() throws Exception
   {
      // create an object model factory
      GenericObjectModelFactory factory = new BookGenericObjectModelFactory();
      unmarshalBook("book-xs.xml", factory);
   }

   public void testMarshallBookDtd() throws Exception
   {
      log.debug("--- " + getName());

      // obtain an instance of Book to marshal
      Book book = createBook();

      // get the output writter to write the XML content
      StringWriter xmlOutput = new StringWriter();

      // get the DTD source
      URL dtdURL = getResource("/xml/book/books.dtd");
      InputStream is = dtdURL.openStream();
      Reader dtdReader = new InputStreamReader(is);

      // create an instance of DTD marshaller
      DtdMarshaller marshaller = new DtdMarshaller();
      marshaller.addBinding("since", new TypeBinding()
      {
         public Object unmarshal(String value)
         {
            // todo: implement unmarshal
            throw new UnsupportedOperationException("unmarshal is not implemented.");
         }

         public String marshal(Object value)
         {
            return SimpleTypeBindings.marshalDate((Calendar)value);
         }
      }
      );

      // map publicId to systemId as it should appear in the resulting XML file
      marshaller.mapPublicIdToSystemId("-//DTD Books//EN", "resources/xml/book/books.dtd");

      // create an instance of ObjectModelProvider with the book instance to be marshalled
      ObjectModelProvider provider = new BookObjectProvider();

      // marshal the book
      marshaller.marshal(dtdReader, provider, book, xmlOutput);

      // close DTD reader
      dtdReader.close();

      String xml = xmlOutput.getBuffer().toString();
      checkMarshalledBook(xml, book);
   }

   public void testMarshallBookXercesXs() throws Exception
   {
      log.debug("--- " + getName());

      System.setProperty(Marshaller.PROP_MARSHALLER, XercesXsMarshaller.class.getName());
      marshallingTest();
   }

   public void testMarshallBookDtdGeneric() throws Exception
   {
      log.debug("--- " + getName());

      // obtain an instance of Book to marshal
      Book book = createBook();

      // get the output writter to write the XML content
      StringWriter xmlOutput = new StringWriter();

      // get the DTD source
      URL dtdURL = getResource("/xml/book/books.dtd");
      InputStream is = dtdURL.openStream();
      Reader dtdReader = new InputStreamReader(is);

      // create an instance of DTD marshaller
      Marshaller marshaller = new DtdMarshaller();

      // map publicId to systemId as it should appear in the resulting XML file
      marshaller.mapPublicIdToSystemId("-//DTD Books//EN", "resources/xml/book/books.dtd");

      // create an instance of ObjectModelProvider with the book instance to be marshalled
      ObjectModelProvider provider = new BookGenericObjectModelProvider();

      // marshal the book
      marshaller.marshal(dtdReader, provider, book, xmlOutput);

      // close DTD reader
      dtdReader.close();

      String xml = xmlOutput.getBuffer().toString();
      if(log.isTraceEnabled())
      {
         log.trace("marshalled with dtd: " + xml);
      }
      checkMarshalledBook(xml, book);
   }

   // Private

   private void marshallingTest()
      throws Exception
   {
      // obtain an instance of Book to marshal
      Book book = createBook();

      // get the output writter to write the XML content
      StringWriter xmlOutput = new StringWriter();

      // create an instance of XML Schema marshaller
      AbstractMarshaller marshaller = (AbstractMarshaller)Marshaller.FACTORY.getInstance();

      // we need to specify what elements are top most (roots) providing namespace URI, prefix and local name
      marshaller.addRootElement("http://example.org/ns/books/", "", "book");

      // declare default namespace
      marshaller.declareNamespace(null, "http://example.org/ns/books/");

      // add schema location by declaring xsi namespace and adding xsi:schemaReader attribute
      marshaller.declareNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
      marshaller.addAttribute("xsi",
         "schemaReader",
         "string",
         "http://example.org/ns/books/ resources/book/books.xsd"
      );

      // create an instance of Object Model Provider with no book
      ObjectModelProvider provider = new BookObjectProvider();

      // marshall Book instance passing it as an argument instead of using the one that is returned by the BookObjectProvider
      marshaller.marshal(getResource("/xml/book/books.xsd").toString(), provider, book, xmlOutput);

      String xml = xmlOutput.getBuffer().toString();
      if(log.isTraceEnabled())
      {
         log.debug("marshalled with " + marshaller.getClass().getName() + ": " + xml);
      }
      checkMarshalledBook(xml, book);
   }

   private void unmarshalBook(String xmlSource, ObjectModelFactory factory) throws Exception
   {
      log.debug("<test-unmarshal-" + xmlSource + '>');

      // get the XML stream
      URL xmlUrl = getResource("/xml/book/" + xmlSource);

      // create unmarshaller
      Unmarshaller unmarshaller = getBookUnmarshaller();

      // let the object model factory to create an instance of Book and populate it with data from XML
      Book book = (Book)unmarshaller.unmarshal(xmlUrl.openStream(), factory, null);

      checkUnmarshalledBook(book);

      log.debug("</test-unmarshal-" + xmlSource + '>');
   }

   private void checkMarshalledBook(String content, Book book)
      throws Exception
   {
      Book unmarshalled = new Book();
      ObjectModelFactory factory = new BookObjectFactory();

      Unmarshaller unmarshaller = getBookUnmarshaller();

      StringReader strReader = new StringReader(content);
      unmarshaller.unmarshal(strReader, factory, unmarshalled);
      strReader.close();

      assertEquals(book, unmarshalled);
   }

   private void checkUnmarshalledBook(Book book)
   {
      log.debug("unmarshalled book: " + book);

      assertEquals("Being a Dog Is a Full-Time Job", book.getTitle());
      assertEquals("Charles M. Schulz", book.getAuthor());
      assertEquals("0836217462", book.getIsbn());
      assertEquals(book.getCharactersTotal(), 2);

      for(Iterator<BookCharacter> iter = book.getCharacters().iterator(); iter.hasNext();)
      {
         BookCharacter character = iter.next();
         final String name = character.getName();
         if(name.equals("Snoopy"))
         {
            assertEquals(character.getFriendOf(), "Peppermint Patty");
            assertEquals(character.getSince(), "1950-10-04");
            assertEquals(character.getQualification(), "extroverted beagle");
         }
         else if(name.equals("Peppermint Patty"))
         {
            assertEquals(character.getFriendOf(), null);
            assertEquals(character.getSince(), "1966-08-22");
            assertEquals(character.getQualification(), "bold, brash and tomboyish");
         }
      }
   }

   private static Unmarshaller getBookUnmarshaller() throws JBossXBException
   {
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      unmarshaller.setEntityResolver(new BooksEntityResolver());
      return unmarshaller;
   }

   private static Book createBook()
   {
      Book book = new Book();
      book.setIsbn("0836217462");
      book.setTitle("Being a Dog Is a Full-Time Job");
      book.setAuthor("Charles M. Schulz");

      BookCharacter character = new BookCharacter();
      character.setName("Snoopy");
      character.setFriendOf("Peppermint Patty");
      character.setSince("1950-10-04");
      character.setQualification("extroverted beagle");
      book.addCharacter(character);

      character = new BookCharacter();
      character.setName("Peppermint Patty");
      character.setSince("1966-08-22");
      character.setQualification("bold, brash and tomboyish");
      book.addCharacter(character);

      return book;
   }

   private static InputStream getResourceStream(String name)
      throws IOException
   {
      URL resURL = findResource(SimpleTestCase.class, name);
      return resURL.openStream();
   }
   private static class BooksEntityResolver implements EntityResolver
   {
      public InputSource resolveEntity(String publicId, String systemId)
         throws SAXException, IOException
      {
         log.debug("resolveEntity, publicId: "+publicId+", systemId: "+systemId);
         if(systemId.endsWith("books.dtd"))
         {
            return new InputSource(getResourceStream("/xml/book/books.dtd"));
         }
         if(systemId.endsWith("books2.dtd"))
         {
            return new InputSource(getResourceStream("/xml/book/books2.dtd"));
         }
         return null;
      }      
   }
}
