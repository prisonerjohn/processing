/* -*- mode: jde; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  PdeEditorHeader - panel that containing the sketch title
  Part of the Processing project - http://Proce55ing.net

  Copyright (c) 2001-03 
  Ben Fry, Massachusetts Institute of Technology and 
  Casey Reas, Interaction Design Institute Ivrea

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License 
  along with this program; if not, write to the Free Software Foundation, 
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class PdeEditorHeader extends Panel /* implements ActionListener*/ {
  static final String SKETCH_TITLER = "sketch";
  static final String USER_TITLER = "user";

  //static final Color primaryColor = Color.white;
  //static final Color secondaryColor = new Color(153, 153, 153);
  //static final Color backgroundColor = new Color(51, 51, 51);

  static Color primaryColor; // = Color.white;
  static Color secondaryColor; // = new Color(153, 153, 153);
  static Color backgroundColor; // = new Color(51, 51, 51);

  PdeEditor editor;

  //private String sketch; // name of current file
  int sketchLeft;
  int sketchRight;
  int sketchTitleLeft;
  //File sketchDir;
  boolean sketchModified;

  //private String user;
  int userLeft;
  int userRight;
  int userTitleLeft;

  Font font;
  FontMetrics metrics;
  int fontAscent;

  Image offscreen;
  int sizeW, sizeH;
  int imageW, imageH;


  public PdeEditorHeader(PdeEditor eddie) { 
    this.editor = eddie; // weird name for listener

    if (primaryColor == null) {
      backgroundColor = PdeBase.getColor("editor.header.bgcolor", 
                                         new Color(51, 51, 51));
      primaryColor = PdeBase.getColor("editor.header.fgcolor.primary", 
                                      new Color(255, 255, 255));
      secondaryColor = PdeBase.getColor("editor.header.fgcolor.secondary", 
                                        new Color(153, 153, 153));
    }

    addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          //System.out.println("got mouse");
          if ((sketchRight != 0) &&
              (e.getX() > sketchLeft) && (e.getX() < sketchRight)) {
            editor.skSaveAs(true);
          }
        }
      });
  }


  public void reset() {
    sketchLeft = 0;
    userLeft = 0;
    update();
  }

  /*
  public void setSketch(String sketch, File sketchDir) {
    this.sketch = sketch;
    this.sketchDir = sketchDir;
    sketchLeft = 0;
    update();
  }

  public void setUser(String user) {
    this.user = user;
    userLeft = 0;
  }
  */


  public void update() {
    paint(this.getGraphics());
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void paint(Graphics screen) {
    if (screen == null) return;
    if (editor.sketchName == null) return;

    Dimension size = getSize();
    if ((size.width != sizeW) || (size.height != sizeH)) {
      // component has been resized

      if ((size.width > imageW) || (size.height > imageH)) {
        // nix the image and recreate, it's too small
        offscreen = null;

      } else {
        // who cares, just resize
        sizeW = size.width; 
        sizeH = size.height;
        userLeft = 0; // reset
      }
    }

    if (offscreen == null) {
      sizeW = size.width;
      sizeH = size.height;
      userLeft = 0; // reset
      imageW = sizeW;
      imageH = sizeH;
      offscreen = createImage(imageW, imageH);
    }

    Graphics g = offscreen.getGraphics();
    if (font == null) {
      font = PdeBase.getFont("editor.header.font",
                             new Font("SansSerif", Font.PLAIN, 12));
      g.setFont(font);
      metrics = g.getFontMetrics();
      fontAscent = metrics.getAscent();
    }

    //if (sketchLeft == 0) {
    sketchTitleLeft = PdeEditor.INSET_SIZE;
    sketchLeft = sketchTitleLeft + 
      metrics.stringWidth(SKETCH_TITLER) + PdeEditor.INSET_SIZE;
    sketchRight = sketchLeft + metrics.stringWidth(editor.sketchName);
    int modifiedLeft = sketchRight + PdeEditor.INSET_SIZE;
    //int modifiedLeft = sketchLeft + 
    //metrics.stringWidth(editor.sketchName) + PdeEditor.INSET_SIZE;

    //sketch = editor.sketchName;
    //if (sketch == null) sketch = "";
    //}

    //if (userLeft == 0) {
    userLeft = sizeW - 20 - metrics.stringWidth(editor.userName);
    userTitleLeft = userLeft - PdeEditor.INSET_SIZE - 
      metrics.stringWidth(USER_TITLER);

    //user = editor.userName;
    //if (user == null) user = "";
    //}

    int baseline = (sizeH + fontAscent) / 2;

    g.setColor(backgroundColor);
    g.fillRect(0, 0, imageW, imageH);

    boolean boringUser = editor.userName.equals("default");

    g.setFont(font); // needs to be set each time
    g.setColor(secondaryColor);
    g.drawString(SKETCH_TITLER, sketchTitleLeft, baseline);
    if (sketchModified) g.drawString("\u00A7", modifiedLeft, baseline);
    if (!boringUser) g.drawString(USER_TITLER, userTitleLeft, baseline);

    g.setColor(primaryColor);
    //g.drawString(sketch, sketchLeft, baseline);
    //String additional = sketchModified ? " \u2020" : "";
    //String additional = sketchModified ? " \u00A4" : "";
    //String additional = sketchModified ? " \u2022" : "";
    g.drawString(editor.sketchName, sketchLeft, baseline);

    //if (!boringUser) g.drawString(user, userLeft, baseline);
    if (!boringUser) g.drawString(editor.userName, userLeft, baseline);

    //g.setColor(fgColor[mode]);
    //g.drawString(message, PdeEditor.INSET_SIZE, (sizeH + fontAscent) / 2);

    screen.drawImage(offscreen, 0, 0, null);
  }


  /*
  protected void setButtonBounds() {
    int top = (sizeH - BUTTON_HEIGHT) / 2;
    int noLeft = sizeW - PdeEditor.INSET_SIZE - BUTTON_WIDTH;
    int yesLeft = noLeft - PdeEditor.INSET_SIZE - BUTTON_WIDTH;

    noButton.setBounds(noLeft, top, BUTTON_WIDTH, BUTTON_HEIGHT);
    yesButton.setBounds(yesLeft, top, BUTTON_WIDTH, BUTTON_HEIGHT);
  }
  */


  public Dimension getPreferredSize() {
      //return new Dimension(300, PdeEditor.GRID_SIZE);
      return getMinimumSize();
  }

  public Dimension getMinimumSize() {
    return new Dimension(300, PdeEditor.GRID_SIZE);
  }

  public Dimension getMaximumSize() {
    return new Dimension(3000, PdeEditor.GRID_SIZE);
  }


  /*
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == noButton) {
      System.out.println("clicked no");

    } else if (e.getSource() == yesButton) {
      System.out.println("clicked yes");
    }
  }
  */
}


  /*
  Color noticeBgColor = new Color(102, 102, 102);
  Color noticeFgColor = new Color(255, 255, 255);

  Color errorBgColor = new Color(102, 26, 0);
  Color errorFgColor = new Color(255, 255, 255);

  Color promptBgColor = new Color(204, 153, 0);
  Color promptFgColor = new COlor(0, 0, 0);
  */
