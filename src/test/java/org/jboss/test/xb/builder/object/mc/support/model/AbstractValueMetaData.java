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
package org.jboss.test.xb.builder.object.mc.support.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jboss.reflect.spi.TypeInfo;
import org.jboss.util.JBossObject;
import org.jboss.util.JBossStringBuilder;
import org.jboss.xb.annotations.JBossXmlNoElements;

/**
 * Plain value.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 59429 $
 */
@XmlType
@JBossXmlNoElements
public class AbstractValueMetaData extends JBossObject
   implements ValueMetaData, Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * The value
    */
   protected Object value;

   /**
    * Create a new plain value
    */
   public AbstractValueMetaData()
   {
   }

   /**
    * Create a new plain value
    *
    * @param value the value
    */
   public AbstractValueMetaData(Object value)
   {
      this.value = value;
   }

   @Override
   @XmlTransient
   public String getClassShortName()
   {
      return super.getClassShortName();
   }
   
   public Object getValue()
   {
      return value;
   }

   public void setValue(Object value)
   {
      this.value = value;
      flushJBossObjectCache();
   }

   @XmlTransient
   public Object getUnderlyingValue()
   {
      return value;
   }

   public Object getValue(TypeInfo info, ClassLoader cl) throws Throwable
   {
      return info != null ? info.convertValue(value) : value;
   }

   public void toString(JBossStringBuilder buffer)
   {
      buffer.append("value=").append(value);
   }

   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(value);
   }
}
