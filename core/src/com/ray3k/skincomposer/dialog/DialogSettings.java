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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.ray3k.skincomposer.Main;
import com.ray3k.skincomposer.Spinner;
import com.ray3k.skincomposer.Spinner.SpinnerStyle;
import com.ray3k.skincomposer.data.AtlasData;
import com.ray3k.skincomposer.data.ProjectData;
import com.ray3k.skincomposer.panel.PanelPreviewProperties;
import com.ray3k.skincomposer.panel.PanelStatusBar;
import com.ray3k.skincomposer.utils.Utils;

public class DialogSettings extends Dialog {
    private Skin skin;
    private SpinnerStyle spinnerStyle;
    private Integer textureWidth;
    private Integer textureHeight;
    private Integer maxUndos;
    private int selectedSkin;
    private boolean useStripWhitespace;

    public DialogSettings(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);
        
        Main.instance.setListeningForKeys(false);
        
        this.skin = skin;
        
        spinnerStyle = new Spinner.SpinnerStyle(skin.get("spinner-minus", Button.ButtonStyle.class), skin.get("spinner-plus", Button.ButtonStyle.class), skin.get("spinner", TextField.TextFieldStyle.class));
        
        textureWidth = ProjectData.instance().getMaxTextureWidth();
        textureHeight = ProjectData.instance().getMaxTextureHeight();
        maxUndos = ProjectData.instance().getMaxUndos();
        selectedSkin = ProjectData.instance().getSelectedSkin();
        useStripWhitespace = ProjectData.instance().getStripWhitespace();
        setFillParent(true);
        
        populate();
    }

    @Override
    protected void result(Object object) {
        super.result(object);
        
        if ((boolean) object) {
            ProjectData.instance().setChangesSaved(false);
            ProjectData.instance().setMaxTextureDimensions(MathUtils.nextPowerOfTwo(textureWidth), MathUtils.nextPowerOfTwo(textureHeight));
            ProjectData.instance().setMaxUndos(maxUndos);
            if (ProjectData.instance().getSelectedSkin() != selectedSkin) {
                showRestartAppDialog();
            }
            ProjectData.instance().setSelectedSkin(selectedSkin);
            ProjectData.instance().setStripWhitespace(useStripWhitespace);
            Main.instance.clearUndoables();
        }
    }

    @Override
    public boolean remove() {
        Main.instance.setListeningForKeys(true);
        PanelStatusBar.instance.message("Settings Updated");
        return super.remove();
    }

    public void populate() {
        Table t = getContentTable();
        
        Label label = new Label("Settings", skin, "title");
        label.setAlignment(Align.center);
        t.add(label).growX().colspan(2);
        
        t.row();
        TextButton textButton = new TextButton("Open temp/log directory", skin, "orange-small");
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                try {
                    Utils.openFileExplorer(Gdx.files.local("temp/"));
                } catch (Exception e) {
                    Gdx.app.error(getClass().getName(), "Error opening temp folder", e);
                    DialogError.showError("Folder Error...", "Error opening temp folder.\n\nOpen log?");
                }
            }
        });
        t.add(textButton).colspan(2);
        
        t.row();
        textButton = new TextButton("Open preferences directory", skin, "orange-small");
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                try {
                    Utils.openFileExplorer(Gdx.files.external(".prefs/"));
                } catch (Exception e) {
                    Gdx.app.error(getClass().getName(), "Error opening preferences folder", e);
                    DialogError.showError("Folder Error...", "Error opening preferences folder.\n\nOpen log?");
                }
            }
        });
        t.add(textButton).colspan(2);
        
        if (ProjectData.instance().areChangesSaved() && ProjectData.instance().getSaveFile().exists()) {
            t.row();
            textButton = new TextButton("Open project/import directory", skin, "orange-small");
            textButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    try {
                        Utils.openFileExplorer(ProjectData.instance().getSaveFile().sibling(ProjectData.instance().getSaveFile().nameWithoutExtension() + "_data"));
                    } catch (Exception e) {
                        Gdx.app.error(getClass().getName(), "Error opening project folder", e);
                        DialogError.showError("Folder Error...", "Error opening project folder\n\nOpen log?");
                    }
                }
            });
            t.add(textButton).colspan(2);
        }
        
        t.row();
        textButton = new TextButton("Repack Texture Atlas", skin, "orange-small");
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Main.instance.showDialogLoading(() -> {
                    try {
                        AtlasData.getInstance().writeAtlas();
                        AtlasData.getInstance().atlasCurrent = true;
                        PanelPreviewProperties.instance.produceAtlas();
                        PanelPreviewProperties.instance.render();
                    } catch (Exception e) {
                        Main.instance.showDialogError("Error", "Unable to write texture atlas to temporary storage!", null);
                        Gdx.app.error(getClass().getName(), "Unable to write texture atlas to temporary storage!", e);
                        DialogError.showError("Atlas Error...", "Unable to write texture atlas to temporary storage.\n\nOpen log?");
                    }
                });
            }
        });
        t.add(textButton).colspan(2);
        
        t.row();
        label = new Label("Textures are rounded up to the next power of 2.", skin);
        t.add(label).colspan(2).padTop(5.0f);
        
        t.row();
        label = new Label("Max Texture Width: ", skin);
        t.add(label).right();
        Spinner spinner = new Spinner(ProjectData.instance().getMaxTextureWidth(), 1.0, true, spinnerStyle);
        spinner.setMinimum(256.0);
        spinner.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                textureWidth = (int) spinner.getValue();
            }
        });
        spinner.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                textureWidth = (int) spinner.getValue();
            }
            
        });
        t.add(spinner).minWidth(150.0f).left();
        
        t.row();
        label = new Label("Max Texture Height: ", skin);
        t.add(label).right();
        Spinner spinner2 = new Spinner(ProjectData.instance().getMaxTextureHeight(), 1.0, true, spinnerStyle);
        spinner2.setMinimum(256.0);
        spinner2.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                textureHeight = (int) spinner2.getValue();
            }
        });
        spinner2.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                textureHeight = (int) spinner2.getValue();
            }
            
        });
        t.add(spinner2).minWidth(150.0f).left();
        
        t.row();
        CheckBox checkBox = new CheckBox(" Strip whitespace on texture export", skin);
        checkBox.setChecked(useStripWhitespace);
        checkBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                useStripWhitespace = checkBox.isChecked();
            }
        });
        t.add(checkBox).colspan(2);
        
        t.row();
        label = new Label("Max Number of Undos: ", skin);
        t.add(label).right().padTop(5.0f);
        Spinner spinner3 = new Spinner(ProjectData.instance().getMaxUndos(), 1.0, true, spinnerStyle);
        spinner3.setMinimum(1.0);
        spinner3.setMaximum(100.0);
        spinner3.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                maxUndos = (int) spinner3.getValue();
            }
        });
        spinner3.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                maxUndos = (int) spinner3.getValue();
            }
            
        });
        t.add(spinner3).minWidth(150.0f).left().padTop(5.0f);
        
        t.row();
        label = new Label("Editor Skin: ", skin);
        t.add(label).right().padTop(5.0f);
        SelectBox selectBox = new SelectBox(skin, "slim");
        selectBox.setItems("Orange Peel UI", "Dark Peel UI");
        selectBox.setSelectedIndex(selectedSkin);
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                selectedSkin = selectBox.getSelectedIndex();
            }
        });
        t.add(selectBox).minWidth(150.0f).left().padTop(5.0f);
        
        button("OK", true);
        button ("Cancel", false);
        key(Keys.ESCAPE, false);
    }

    private void showRestartAppDialog() {
        Dialog dialog = new Dialog("Restart App", skin, "dialog");
        Label label = new Label("Please restart the application\nto apply changes to the user interface.", skin);
        label.setAlignment(Align.center);
        dialog.text(label);
        dialog.getContentTable().getCells().first().pad(10.0f);
        dialog.button("OK");
        dialog.show(getStage());
    }
}
