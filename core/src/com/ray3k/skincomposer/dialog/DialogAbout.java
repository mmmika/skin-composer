/*******************************************************************************
 * MIT License
 * 
 * Copyright (c) 2016 Raymond Buckley
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.ray3k.skincomposer.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.ray3k.skincomposer.HandListener;
import com.ray3k.skincomposer.Main;

public class DialogAbout extends Dialog {
    public DialogAbout(Skin skin, String windowStyleName) {
        super("About", skin, windowStyleName);
        
        Main.instance.setListeningForKeys(false);
        
        key(Keys.ENTER, true);
        key(Keys.ESCAPE, false);
        getTitleLabel().setAlignment(Align.center);
        Table table = getContentTable();
        table.defaults().pad(10.0f);
        Label label = new Label("Skin Composer is developed by Raeleus for the LibGDX community\nVersion " + Main.VERSION + "\nCopyright © Raymond \"Raeleus\" Buckley 2016", skin);
        label.setAlignment(Align.center);
        table.add(label).padBottom(0);
        table.row();
        TextButton button = new TextButton("ray3k.wordpress.com/software/skin-composer-for-libgdx/", skin, "link");
        button.addListener(HandListener.get());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Gdx.net.openURI("https://ray3k.wordpress.com/software/skin-composer-for-libgdx/");
            }
        });
        table.add(button).padTop(0);
        button("Close");
        table.setWidth(200);
    }

    @Override
    public boolean remove() {
        Main.instance.setListeningForKeys(true);
        return super.remove();
    }
}
