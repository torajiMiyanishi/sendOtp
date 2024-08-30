package jp.soars.otp;

import org.opentripplanner.openstreetmap.model.OSMNode;
import org.opentripplanner.openstreetmap.model.OSMRelation;
import org.opentripplanner.openstreetmap.model.OSMWay;
import org.opentripplanner.openstreetmap.services.OpenStreetMapContentHandler;

public class TSimpleOpenStreetMapContentHandler implements OpenStreetMapContentHandler {
    @Override
    public void addNode(OSMNode node) {
    }

    @Override
    public void addWay(OSMWay way) {
    }

    @Override
    public void addRelation(OSMRelation relation) {
    }

    @Override
    public void doneFirstPhaseRelations() {
    }

    @Override
    public void doneSecondPhaseWays() {
    }

    @Override
    public void doneThirdPhaseNodes() {
    }
}
