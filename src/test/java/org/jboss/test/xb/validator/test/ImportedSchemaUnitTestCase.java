/*
* JBoss, Home of Professional Open Source
* Copyright 2009, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.xb.validator.test;

import org.jboss.test.xb.builder.AbstractBuilderTest;
import org.jboss.test.xb.validator.support.ImportedSchema;
import org.jboss.test.xb.validator.support.ImportingSchema;
import org.jboss.xb.binding.resolver.MultiClassSchemaResolver;
import org.jboss.xb.binding.resolver.MutableSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.builder.JBossXBBuilder;
import org.jboss.xb.util.DefaultSchemaBindingValidator;
import org.xml.sax.InputSource;

/**
 * A ImportedSchemaUnitTestCase.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class ImportedSchemaUnitTestCase extends AbstractBuilderTest
{
   public ImportedSchemaUnitTestCase(String name)
   {
      super(name);
   }

   public void testMain() throws Exception
   {
      String xsd = findXML("ImportedSchemaUnitTestCase.xsd");
      InputSource xsdIs = new InputSource(xsd);

      SchemaBinding schema = JBossXBBuilder.build(ImportingSchema.class);
      
      MutableSchemaResolver resolver = new MultiClassSchemaResolver();
      resolver.mapURIToClass("urn:jboss:xb:test:imported", ImportedSchema.class);
      
      DefaultSchemaBindingValidator validator = new DefaultSchemaBindingValidator(resolver);
      validator.validate(xsdIs, schema);
   }
}
