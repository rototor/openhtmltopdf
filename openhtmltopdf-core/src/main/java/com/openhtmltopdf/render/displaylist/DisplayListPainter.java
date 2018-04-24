package com.openhtmltopdf.render.displaylist;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import com.openhtmltopdf.layout.CollapsedBorderSide;
import com.openhtmltopdf.layout.InlinePaintable;
import com.openhtmltopdf.newtable.TableCellBox;
import com.openhtmltopdf.render.BlockBox;
import com.openhtmltopdf.render.DisplayListItem;
import com.openhtmltopdf.render.OperatorClip;
import com.openhtmltopdf.render.OperatorSetClip;
import com.openhtmltopdf.render.RenderingContext;
import com.openhtmltopdf.render.displaylist.DisplayListContainer.DisplayListPageContainer;

public class DisplayListPainter {
	private void paintBackgroundAndBorders(RenderingContext c, List<DisplayListItem> blocks,
			Map<TableCellBox, List<CollapsedBorderSide>> collapsedTableBorders) {

		for (DisplayListItem dli : blocks) {
			if (dli instanceof OperatorClip) {
				OperatorClip clip = (OperatorClip) dli;
				c.getOutputDevice().clip(clip.getClip());
			} else if (dli instanceof OperatorSetClip) {
				OperatorSetClip setClip = (OperatorSetClip) dli;
				c.getOutputDevice().setClip(setClip.getSetClipShape());
			} else {
				BlockBox box = (BlockBox) dli;

				box.paintBackground(c);
				box.paintBorder(c);

				if (collapsedTableBorders != null && box instanceof TableCellBox) {
					TableCellBox cell = (TableCellBox) box;

					if (cell.hasCollapsedPaintingBorder()) {
						List<CollapsedBorderSide> borders = collapsedTableBorders.get(cell);

						if (borders != null) {
							for (CollapsedBorderSide border : borders) {
								border.getCell().paintCollapsedBorder(c, border.getSide());
							}
						}
					}
				}
			}
		}
	}

	private void paintListMarkers(RenderingContext c, List<DisplayListItem> blocks) {
		for (DisplayListItem dli : blocks) {
			if (dli instanceof OperatorClip) {
				OperatorClip clip = (OperatorClip) dli;
				c.getOutputDevice().clip(clip.getClip());
			} else if (dli instanceof OperatorSetClip) {
				OperatorSetClip setClip = (OperatorSetClip) dli;
				c.getOutputDevice().setClip(setClip.getSetClipShape());
			} else {
				((BlockBox) dli).paintListMarker(c);
			}
		}
	}

	private void paintInlineContent(RenderingContext c, List<DisplayListItem> inlines) {
		for (DisplayListItem dli : inlines) {
			if (dli instanceof OperatorClip) {
				OperatorClip clip = (OperatorClip) dli;
				c.getOutputDevice().clip(clip.getClip());
			} else if (dli instanceof OperatorSetClip) {
				OperatorSetClip setClip = (OperatorSetClip) dli;
				c.getOutputDevice().setClip(setClip.getSetClipShape());
			} else {
				InlinePaintable paintable = (InlinePaintable) dli;
				paintable.paintInline(c);
			}
		}
	}

	private void paintReplacedElements(RenderingContext c, List<DisplayListItem> replaceds) {
		for (DisplayListItem dli : replaceds) {
			if (dli instanceof OperatorClip) {
				OperatorClip clip = (OperatorClip) dli;
				c.getOutputDevice().clip(clip.getClip());
			} else if (dli instanceof OperatorSetClip) {
				OperatorSetClip setClip = (OperatorSetClip) dli;
				c.getOutputDevice().setClip(setClip.getSetClipShape());
			} else {
				BlockBox box = (BlockBox) dli;
				paintReplacedElement(c, box);
			}
		}
	}
	
    private void paintReplacedElement(RenderingContext c, BlockBox replaced) {
        
    	Rectangle contentBounds = replaced.getContentAreaEdge(
                replaced.getAbsX(), replaced.getAbsY(), c);
    	
        // Minor hack:  It's inconvenient to adjust for margins, border, padding during
        // layout so just do it here.
        Point loc = replaced.getReplacedElement().getLocation();
        if (contentBounds.x != loc.x || contentBounds.y != loc.y) {
            replaced.getReplacedElement().setLocation(contentBounds.x, contentBounds.y);
        }
        
        c.getOutputDevice().paintReplacedElement(c, replaced);
    }

	public void paint(RenderingContext c, DisplayListPageContainer pageOperations) {
		for (DisplayListOperation op : pageOperations.getOperations()) {

			if (op instanceof PaintRootElementBackground) {

				PaintRootElementBackground dlo = (PaintRootElementBackground) op;
				dlo.getRoot().paintRootElementBackground(c);

			} else if (op instanceof PaintLayerBackgroundAndBorder) {

				PaintLayerBackgroundAndBorder dlo = (PaintLayerBackgroundAndBorder) op;
				dlo.getMaster().paintBackground(c);
				dlo.getMaster().paintBorder(c);

			} else if (op instanceof PaintReplacedElement) {

				PaintReplacedElement dlo = (PaintReplacedElement) op;
				paintReplacedElement(c, dlo.getMaster());

			} else if (op instanceof PaintBackgroundAndBorders) {

				PaintBackgroundAndBorders dlo = (PaintBackgroundAndBorders) op;
				paintBackgroundAndBorders(c, dlo.getBlocks(), dlo.getCollapedTableBorders());

			} else if (op instanceof PaintListMarkers) {

				PaintListMarkers dlo = (PaintListMarkers) op;
				paintListMarkers(c, dlo.getBlocks());

			} else if (op instanceof PaintInlineContent) {

				PaintInlineContent dlo = (PaintInlineContent) op;
				paintInlineContent(c, dlo.getInlines());

			} else if (op instanceof PaintReplacedElements) {

				PaintReplacedElements dlo = (PaintReplacedElements) op;
				paintReplacedElements(c, dlo.getReplaceds());

			} else {

			}
		}
	}

}
