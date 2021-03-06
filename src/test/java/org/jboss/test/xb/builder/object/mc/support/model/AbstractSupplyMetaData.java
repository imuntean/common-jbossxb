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

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.jboss.util.JBossObject;
import org.jboss.util.JBossStringBuilder;

/**
 * A supply.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 59429 $
 */
@XmlType
public class AbstractSupplyMetaData extends JBossObject
   implements SupplyMetaData, Serializable
{
   private static final long serialVersionUID = 1L;

   /** The supply */
   protected Object supply;

   /**
    * Create a new supply
    */
   public AbstractSupplyMetaData()
   {
   }

   /**
    * Create a new supply
    * 
    * @param supply the supply
    */
   public AbstractSupplyMetaData(Object supply)
   {
      this.supply = supply;
   }
   
   /**
    * Set the supply
    * 
    * @param supply the supply
    */
   @XmlValue
   public void setSupply(Object supply)
   {
      this.supply = supply;
      flushJBossObjectCache();
   }

   public Object getSupply()
   {
      return supply;
   }
   
   public void toString(JBossStringBuilder buffer)
   {
      buffer.append("supply=").append(supply);
   }
   
   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(supply);
   }
}
