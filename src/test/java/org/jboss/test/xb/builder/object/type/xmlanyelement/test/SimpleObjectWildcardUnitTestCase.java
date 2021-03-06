/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.xb.builder.object.type.xmlanyelement.test;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import junit.framework.Test;

import org.jboss.test.xb.builder.AbstractBuilderTest;
import org.jboss.test.xb.builder.object.type.xmlanyelement.support.SimpleJavaBean;
import org.jboss.test.xb.builder.object.type.xmlanyelement.support.SimpleObjectWildcard;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ParticleBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ParticleHandler;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SequenceBinding;
import org.jboss.xb.binding.sunday.unmarshalling.TermBinding;
import org.jboss.xb.binding.sunday.unmarshalling.TypeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.WildcardBinding;
import org.jboss.xb.builder.JBossXBBuilder;
import org.jboss.xb.builder.runtime.PropertyWildcardHandler;

/**
 * SimpleObjectWildcardUnitTestCase.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class SimpleObjectWildcardUnitTestCase extends AbstractBuilderTest
{
   public static Test suite()
   {
      return suite(SimpleObjectWildcardUnitTestCase.class);
   }
   
   public SimpleObjectWildcardUnitTestCase(String name)
   {
      super(name);
   }

   public void testUnmarshalWildcard() throws Exception
   {
      SimpleObjectWildcard result = unmarshalObject(SimpleObjectWildcard.class, SimpleObjectWildcard.class, SimpleJavaBean.class);
      Object object = result.getWildcard();
      assertNotNull(object);
      assertTrue(object instanceof SimpleJavaBean);
   }

   public void testWildcardBinding() throws Exception
   {
      SchemaBinding schemaBinding = JBossXBBuilder.build(SimpleObjectWildcard.class);
      assertNotNull(schemaBinding);
      
      QName qName = new QName(XMLConstants.NULL_NS_URI, "simple-object-wildcard");
      ElementBinding element = schemaBinding.getElement(qName);
      assertNotNull(element);
      TypeBinding type = element.getType();
      assertNotNull(type);
      ParticleBinding particle = type.getParticle();
      assertNotNull(particle);
      TermBinding term = particle.getTerm();
      assertNotNull(term);
      term = assertSingleSequence(term);
      assertTrue(term instanceof WildcardBinding);
      WildcardBinding wildcardBinding = type.getWildcard();
      assertNotNull(wildcardBinding);
      assertTrue(term == wildcardBinding);
      assertTrue(wildcardBinding.isProcessContentsStrict());
      ParticleHandler particleHandler = wildcardBinding.getWildcardHandler();
      assertNotNull(particleHandler);
      assertTrue(particleHandler instanceof PropertyWildcardHandler);
   }
}
