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
package org.jboss.test.xb.builder.object.beanaccessmode.support;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.xb.annotations.JBossXmlAccessMode;
import org.jboss.xb.annotations.JBossXmlConstants;
import org.jboss.xb.annotations.JBossXmlModelGroup;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * A PropertyAccessModeType.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
@JBossXmlSchema(accessMode = JBossXmlAccessMode.ALL)
@XmlRootElement(name="root")
public class PublicMemberAccessModeGroupOverride extends SomeType
{
   public PropertyAccessModeGroup e;
   
   @JBossXmlModelGroup(name="all", accessMode=JBossXmlAccessMode.PUBLIC_MEMBER, kind=JBossXmlConstants.MODEL_GROUP_ALL)
   public static class PropertyAccessModeGroup
   {
      private String property;
      public String publicField;
      private String privateField;
      
      public String getProperty()
      {
         return property;
      }
      
      public void setProperty(String property)
      {
         this.property = property;
      }
      
      public String privateField()
      {
         return privateField;
      }
   }
}
