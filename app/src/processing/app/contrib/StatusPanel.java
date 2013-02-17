/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2013 The Processing Foundation
  Copyright (c) 2011-12 Ben Fry and Casey Reas

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along 
  with this program; if not, write to the Free Software Foundation, Inc.
  59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package processing.app.contrib;

import java.awt.Color;

import javax.swing.JLabel;


class StatusPanel extends JLabel {
  
//  public StatusPanel() {
//    setBackground(null);
//    setBorder(null);
//  }
  
  void setMessage(String message) {
    setForeground(Color.BLACK);
    setText(message);
    repaint();
  }
  
  void setErrorMessage(String message) {
    setForeground(Color.RED);
    setText(message);
    repaint();
  }
}


/*
interface ErrorWidget {
  void setErrorMessage(String msg);
}


class StatusPanel extends JPanel implements ErrorWidget {
  String errorMessage;

  StatusPanel() {
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        clearErrorMessage();
      }
    });
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    g.setFont(new Font("SansSerif", Font.PLAIN, 10));
    int baseline = (getSize().height + g.getFontMetrics().getAscent()) / 2;

    if (contribListing.isDownloadingListing()) {
      g.setColor(Color.black);
      g.drawString("Downloading software listing...", 2, baseline);
      setVisible(true);
    } else if (errorMessage != null) {
      g.setColor(Color.red);
      g.drawString(errorMessage, 2, baseline);
      setVisible(true);
    } else {
      setVisible(false);
    }
  }

  public void setErrorMessage(String message) {
    errorMessage = message;
    setVisible(true);

    JPanel placeholder = getPlaceholder();
    Dimension d = getPreferredSize();
    if (Base.isWindows()) {
      d.height += 5;
      placeholder.setPreferredSize(d);
    }
    placeholder.setVisible(true);
  }

  void clearErrorMessage() {
    errorMessage = null;
    repaint();

    getPlaceholder().setVisible(false);
  }
}  
*/