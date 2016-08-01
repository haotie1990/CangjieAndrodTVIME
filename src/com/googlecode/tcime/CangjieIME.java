/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.tcime;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import java.util.List;
import android.view.KeyEvent;

/**
 * Cangjie input method.
 */

public class CangjieIME extends AbstractIME {
	private CangjieEditor cangjieEditor;
	private CangjieDictionary cangjieDictionary;

	@Override
	protected KeyboardSwitch createKeyboardSwitch(Context context) {
		return new KeyboardSwitch(context, R.xml.cangjie);
	}

	@Override
	protected Editor createEditor() {
		cangjieEditor =  new CangjieEditor();
		return cangjieEditor;
	}

	@Override
	protected WordDictionary createWordDictionary(Context context) {
		cangjieDictionary = new CangjieDictionary(context);
		return cangjieDictionary;
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		if (handleCangjieSimplified(primaryCode)) {
			return;
		}
		super.onKey(primaryCode, keyCodes);
	}

	private boolean handleCangjieSimplified(int keyCode) {
		if (keyCode == Keyboard.KEYCODE_SHIFT) {
		  if ((inputView != null) && inputView.toggleCangjieSimplified()) {
			boolean simplified = inputView.isCangjieSimplified();
			cangjieEditor.setSimplified(simplified);
			cangjieDictionary.setSimplified(simplified);
			escape();
			return true;
		  }
		}
		return false;
	}
	
	private int nCurKeyboardKeyNums;
	private Keyboard nCurrentKeyboard;
	private List<Keyboard.Key> nKeys;
	private int nLastKeyIndex = 0;
	private SoftKeyboardView   mInputView; 

	public static int RestMustOnKeyboard = 1;
	public static int RestMustOnCandidate = 0;
	public static int  nLastCandidateIndex = 0;

	private boolean isHandleKey = false;//已经处理过的按键，则在onKeyUp方法里不再交由父类处理
	
	private void setFields() {
	    if (null == mInputView) 
	    	return;
	    nCurrentKeyboard = mInputView.getKeyboard();
	    nKeys = nCurrentKeyboard.getKeys();
	    nCurKeyboardKeyNums = nKeys.size();
	    nLastKeyIndex = mInputView.getLastKeyIndex();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (isHandleKey) {
			isHandleKey = false;
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		mInputView=inputView;		  
		  if (!isInputViewShown()) {       
	          return super.onKeyDown(keyCode, event);
	      }	  	  	
		  if((!candidatesContainer.isShown())){
			  RestMustOnKeyboard=1;
			  
		  }
		  
		 if(RestMustOnKeyboard==0){
		  switch (keyCode) {                   
		      case KeyEvent.KEYCODE_DPAD_DOWN:   	
		    	  RestMustOnKeyboard=1;
		    	  candidatesContainer.candidateView_invalidate();
		    	  mInputView.invalidate();
		    	  isHandleKey = true; 
		          return true;
		      case KeyEvent.KEYCODE_DPAD_UP:
		    	  isHandleKey = true;
		          return true;
		      case KeyEvent.KEYCODE_DPAD_LEFT:
		    	  if((nLastCandidateIndex>=1)&&(nLastCandidateIndex<=5)){
		        	  nLastCandidateIndex--;
		          }else if(nLastCandidateIndex==0&&(candidatesContainer.leftArrow.isEnabled())){
		        	  candidatesContainer.showPage(candidatesContainer.currentPage -1);
		        	  nLastCandidateIndex=0;
		          }
		    	  candidatesContainer.setHilightIndex(nLastCandidateIndex);
		    	  candidatesContainer.candidateView_invalidate();
		    	  isHandleKey = true;
		          return true;
		      case KeyEvent.KEYCODE_DPAD_RIGHT:
		    	  if((nLastCandidateIndex>=0)&&(nLastCandidateIndex<=4)){
		    	      nLastCandidateIndex++;
		    	  }else if(nLastCandidateIndex==5&&(candidatesContainer.rightArrow.isEnabled())){
		    		  candidatesContainer.showPage(candidatesContainer.currentPage + 1);
		    		  nLastCandidateIndex=0;
		    	  }
		    	  candidatesContainer.setHilightIndex(nLastCandidateIndex);
		    	  candidatesContainer.candidateView_invalidate();
		    	  isHandleKey = true;
		          return true;
		      case KeyEvent.KEYCODE_DPAD_CENTER:
		      case KeyEvent.KEYCODE_ENTER:
		  		  candidatesContainer.setHilightIndex(nLastCandidateIndex);
		  		  candidatesContainer.pickHighlighted();
		    	  if((!candidatesContainer.isShown())){
		    		  RestMustOnKeyboard=1;
		    		  mInputView.invalidate();
		    	  }else {
				      nLastCandidateIndex = 0;
				      candidatesContainer.setHilightIndex(nLastCandidateIndex);
				  }
		    	  isHandleKey = true;
		    	  return true;
		     } 
		 } 
	  
		 if(RestMustOnKeyboard==1){
		    switch (keyCode) {                   
		        case KeyEvent.KEYCODE_DPAD_DOWN:   	        
		            setFields();
		            if (nLastKeyIndex >= nCurKeyboardKeyNums - 1) {
		                if (null == mInputView) return false;
		                mInputView.setLastKeyIndex(0);
		            } else {
		                int[] nearestKeyIndices = nCurrentKeyboard.getNearestKeys(
		                        nKeys.get(nLastKeyIndex).x, nKeys.get(nLastKeyIndex).y);
		                
		                for (int index : nearestKeyIndices) {
		                    if (nLastKeyIndex < index) {
		                        Key nearKey = nKeys.get(index);
		                        Key lastKey = nKeys.get(nLastKeyIndex);
		                        if (
		                                ((lastKey.x >= nearKey.x) // left side compare
		                                  && (lastKey.x < (nearKey.x + nearKey.width)))
		                              || (((lastKey.x + lastKey.width) > nearKey.x) // right side compare
		                                   && ((lastKey.x + lastKey.width) <= (nearKey.x + nearKey.width)))
		                            ) 
		                        {
		                            mInputView.setLastKeyIndex(index);
		                            break;
		                        } 
		                    } 
		                }
		            }
		            mInputView.invalidate();
		            isHandleKey = true;
		            return true;
		        case KeyEvent.KEYCODE_DPAD_UP:
		            setFields();
		            if((nLastKeyIndex>=0)&&(nLastKeyIndex<=8)&&(candidatesContainer.isShown())){//sunfabo 2013.11.28
		            	RestMustOnKeyboard=0;
		            	mInputView.invalidate();
		            	nLastCandidateIndex=0;
		            	candidatesContainer.setHilightIndex(nLastCandidateIndex);
		            	candidatesContainer.candidateView_invalidate();
		                return true;
		            }
		            
		            if (nLastKeyIndex <= 0) {
		                if (null == mInputView) return false;
		                mInputView.setLastKeyIndex(nCurKeyboardKeyNums - 1); 
		            } else {
		                int[] nearestKeyIndices = nCurrentKeyboard.getNearestKeys(
		                        nKeys.get(nLastKeyIndex).x, nKeys.get(nLastKeyIndex).y);
		                
		                for (int i = nearestKeyIndices.length - 1; i >= 0; i--) {
		                    int index = nearestKeyIndices[i];
		                    if (nLastKeyIndex > index) {
		                        Key nearKey = nKeys.get(index);// get the next key
		                        Key nextNearKey = nKeys.get(index + 1);
		                        Key lastKey = nKeys.get(nLastKeyIndex);// get current displayed
		                        if (    
		                                ((lastKey.x >= nearKey.x) && 
		                                    (lastKey.x < (nearKey.x + nearKey.width)) &&
		                                    (((lastKey.x + lastKey.width) <= (nextNearKey.x + nextNearKey.width)) 
		                                        || ((lastKey.x + lastKey.width) > nextNearKey.x))) 
		                            ) 
		                        {
		                            mInputView.setLastKeyIndex(index);
		                            break;
		                        } 
		                    } 
		                }
		            }
		            mInputView.invalidate();
		            isHandleKey = true;
		            return true;
		        case KeyEvent.KEYCODE_DPAD_LEFT:
		            setFields();
		            if (nLastKeyIndex <= 0) {
		                if (null == mInputView) return false;
		                mInputView.setLastKeyIndex(nCurKeyboardKeyNums - 1);
		            } else {
		                nLastKeyIndex--;
		                mInputView.setLastKeyIndex(nLastKeyIndex);
		            }
		            mInputView.invalidate();
		            isHandleKey = true;
		            return true;
		        case KeyEvent.KEYCODE_DPAD_RIGHT:
		            setFields();
		            if (nLastKeyIndex >= nCurKeyboardKeyNums - 1) {
		                if (null == mInputView) return false;
		                mInputView.setLastKeyIndex(0);
		            } else {
		                nLastKeyIndex++;
		                mInputView.setLastKeyIndex(nLastKeyIndex);
		            }
		            mInputView.invalidate();
		            isHandleKey = true;
		            return true;
		        case KeyEvent.KEYCODE_DPAD_CENTER:
		            if (null == mInputView) return false;
		            setFields();
		            int curKeyCode = nKeys.get(nLastKeyIndex).codes[0];
		            onKey(curKeyCode, null);
		            isHandleKey = true;
		            return true;
		    }  
		} 
	    return super.onKeyDown(keyCode, event);
	}
}







