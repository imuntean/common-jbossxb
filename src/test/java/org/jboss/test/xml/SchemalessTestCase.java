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

import junit.framework.TestCase;
import org.jboss.test.xml.person.Person;
import org.jboss.xb.binding.SchemalessMarshaller;
import org.jboss.xb.binding.SchemalessObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 37406 $</tt>
 */
public class SchemalessTestCase
   extends TestCase
{
   public SchemalessTestCase()
   {
   }

   public SchemalessTestCase(String localName)
   {
      super(localName);
   }

   public void testSchemalessMarshalling() throws Exception
   {
      Person person = Person.newInstance();
      StringWriter writer = new StringWriter();

      SchemalessMarshaller marshaller = new SchemalessMarshaller();
      marshaller.marshal(person, writer);

      StringReader reader = new StringReader(writer.getBuffer().toString());
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      SchemalessObjectModelFactory factory = new SchemalessObjectModelFactory();
      Person unmarshalled = (Person)unmarshaller.unmarshal(reader, factory, null);

      assertEquals(person, unmarshalled);
   }
}
