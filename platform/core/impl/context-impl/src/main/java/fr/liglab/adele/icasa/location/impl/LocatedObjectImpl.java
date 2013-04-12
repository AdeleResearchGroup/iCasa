/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.location.impl;

import java.util.ArrayList;
import java.util.List;

import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Position;

public abstract class LocatedObjectImpl implements LocatedObject {

	private Position m_position;
	
	private List<LocatedObject> attachedObjects = new ArrayList<LocatedObject>();
	
	public LocatedObjectImpl(Position position) {
		m_position = position.clone();
	}
	
	@Override
   public Position getCenterAbsolutePosition() {
	   return m_position.clone();
   }

	@Override
   public void setCenterAbsolutePosition(Position position) {
		int deltaX = position.x - getCenterAbsolutePosition().x ;
		int deltaY = position.y - getCenterAbsolutePosition().y;
		m_position = position.clone();		
		moveAttachedObjects(deltaX, deltaY);
   }
		
	protected void moveAttachedObjects(int deltaX, int deltaY) {
		for (LocatedObject object : attachedObjects) {
	      int newX = object.getCenterAbsolutePosition().x + deltaX;
	      int newY = object.getCenterAbsolutePosition().y + deltaY;
	      Position objectPosition = new Position(newX, newY);
	      object.setCenterAbsolutePosition(objectPosition);
      }
	}

	@Override
   public void attachObject(LocatedObject object) {
		if (object==this)
			return;
		
		attachedObjects.add(object);
        notifyAttachedObject(object);
   }

	@Override
   public void detachObject(LocatedObject object) {
		if (object==this)
			return;
		
		attachedObjects.remove(object);
        notifyDetachedObject(object);
   }

   protected abstract void notifyAttachedObject(LocatedObject attachedObject);

   protected abstract void notifyDetachedObject(LocatedObject attachedObject);

}
