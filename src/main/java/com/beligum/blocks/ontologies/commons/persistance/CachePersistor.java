package com.beligum.blocks.ontologies.commons.persistance;

import com.beligum.base.utils.Logger;
import com.beligum.blocks.ontologies.commons.wikidata.GenericModelSubject;
import com.beligum.blocks.ontologies.commons.wikidata.WikidataResourceInfo;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Bram on 21/04/17.
 */
public class CachePersistor
{

    private static class LazyHolder
    {
        private static final CachePersistor INSTANCE = new CachePersistor();
    }

    public static CachePersistor getInstance()
    {

        return LazyHolder.INSTANCE;
    }
    /**
     *
     * @param tablename will empty the table with  this name from the wikidata database
     */
    public void emptyTable(String tablename)
    {
        String sqlQuery = "DELETE FROM " + tablename;

        try (Connection conn = SQLiteJDBCDriverConnection.getInstance().getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            stmt.execute();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("error in deleting. This shouldn't happen ");
            throw new RuntimeException(ex.getMessage());
        }

    }

public void putWikidataAndModel(String wikidataId, String subject, String predicate, String objectLabel, String objectLanguage)
{
    String sqlQuery = "INSERT INTO wikidatamodel "
                      + "(id, subject, predicate, objectlabel, objectlanguage) VALUES"
                      + "(?,?,?,?,?)";
    try (Connection conn = SQLiteJDBCDriverConnection.getInstance().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
        conn.setAutoCommit(false);

        stmt.setString(1, wikidataId);
        stmt.setString(2, subject);
        stmt.setString(3, predicate);
        stmt.setString(4, objectLabel);
        stmt.setString(5, objectLanguage);

        stmt.executeUpdate();
        conn.commit();
    }
    catch (Exception ex) {
        ex.printStackTrace();
    }

}

public List<GenericModelSubject> getWikiModel(String wikidataId){
    List<GenericModelSubject> retVal = null;
    String sqlQuery = "SELECT subject, predicate, objectlabel, objectlanguage FROM wikidatamodel WHERE id = ?";
    try (Connection conn = SQLiteJDBCDriverConnection.getInstance().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
        stmt.setString(1, wikidataId);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            if(retVal == null){
                retVal = new ArrayList<>();
            }
            retVal.add(new GenericModelSubject(rs.getString("subject"), rs.getString("predicate"),
                                               rs.getString("objectlabel"), rs.getString("objectlanguage")));
        }
    }
    catch (Exception ex) {
        ex.printStackTrace();
    }
    return retVal;
}

    public WikidataResourceInfo getwikiresourceinfo(String wikidataId){
        WikidataResourceInfo retVal = null;
        String sqlQuery = "SELECT lang, dataLabel, pic, sitelink, resourcetype FROM wikiresourceinfo WHERE id = ?";
        try (Connection conn = SQLiteJDBCDriverConnection.getInstance().getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            stmt.setString(1, wikidataId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                retVal = new WikidataResourceInfo();
                if(rs.getString("dataLabel") != null){
                    retVal.setLabel(rs.getString("dataLabel"));
                }
                if(rs.getString("dataLabel") != null){
                    retVal.setLabel(rs.getString("dataLabel"));
                }
                if(rs.getString("lang") != null){
                    retVal.setLanguage(Locale.forLanguageTag(rs.getString("lang")));
                }
                if(rs.getString("pic") != null){
                    retVal.setImage(rs.getString("pic"));
                }
                if(rs.getString("sitelink") != null){
                    retVal.setLink(new URI(rs.getString("sitelink")));
                }
                if(rs.getString("resourcetype") != null){
                    retVal.setResourceType(new URI(rs.getString("resourcetype")));
                }
                retVal.setWikidataId(wikidataId);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return retVal;
    }

    public void putwikiresourceinfo(WikidataResourceInfo wikidataresourceinfo)
    {
        String sqlQuery = "INSERT INTO wikiresourceinfo "
                          + "(id, dataLabel, lang, pic, siteLink, resourcetype) VALUES"
                          + "(?,?,?,?,?,?)";
        try (Connection conn = SQLiteJDBCDriverConnection.getInstance().getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            conn.setAutoCommit(false);

            if(wikidataresourceinfo.getWikidataId() != null) stmt.setString(1, wikidataresourceinfo.getWikidataId() );
            if(wikidataresourceinfo.getLabel() != null)stmt.setString(2, wikidataresourceinfo.getLabel().toString() );
            if(wikidataresourceinfo.getLanguage() != null)stmt.setString(3, wikidataresourceinfo.getLanguage().toLanguageTag() );
            if(wikidataresourceinfo.getImage() != null)stmt.setString(4, wikidataresourceinfo.getImage().toString() );
            if(wikidataresourceinfo.getLink() != null)stmt.setString(5, wikidataresourceinfo.getLink().toString() );
            if(wikidataresourceinfo.getResourceType() != null)stmt.setString(6, wikidataresourceinfo.getResourceType().toString() );

            stmt.executeUpdate();
            conn.commit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }



}


