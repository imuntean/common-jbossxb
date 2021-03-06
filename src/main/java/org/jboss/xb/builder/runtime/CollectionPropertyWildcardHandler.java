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
package org.jboss.xb.builder.runtime;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.xb.binding.sunday.unmarshalling.ParticleBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ParticleHandler;
import org.jboss.xb.spi.BeanAdapter;
import org.xml.sax.Attributes;

/**
 * CollectionPropertyWildcardHandler.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class CollectionPropertyWildcardHandler extends CollectionPropertyHandler implements ParticleHandler
{
   /**
    * Create a new CollectionPropertyWildcardHandler.
    * 
    * @param propertyInfo the property
    * @param propertyType the property type
    * @throws IllegalArgumentException for a null property
    */
   public CollectionPropertyWildcardHandler(PropertyInfo propertyInfo, TypeInfo propertyType)
   {
      super(propertyInfo, propertyType);
   }

   public Object startParticle(Object parent, QName elementName, ParticleBinding particle, Attributes attrs, NamespaceContext nsCtx)
   {
      return parent;
   }

   public void setParent(Object parent, Object o, QName elementName, ParticleBinding particle, ParticleBinding parentParticle)
   {
      BeanAdapter beanAdapter = (BeanAdapter) parent;
      AbstractPropertyHandler propertyHandler = beanAdapter.getPropertyHandler(elementName);
      if (propertyHandler != null)
         propertyHandler.doHandle(parent, o, elementName);
      else
         doHandle(parent, o, elementName);
   }

   public Object endParticle(Object o, QName elementName, ParticleBinding particle)
   {
      return o;
   }
}
