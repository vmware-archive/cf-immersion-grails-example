import java.util.NoSuchElementException;

/**
 * This class
 * @author Antoine Roux
 * @author Jun Chen
 *
 */
class MigrationBuilder {

	
    /** 
     * The map of the Migrations that were parsed. 
     * Key: Migration group. 
     * Value: list of Migration positions 
     */
    def migrations = [:]
    
    /** The list of changeSets that were parsed */
    def changeSets = []
    /** 
     * The list of all changes that were parsed.
     * Key: id of the MigrationItem.
     * Value: value of the MigrationItem. 
     */
    def changes = [:]
    
    /**
     * The name of the closure being currently parsed. "items" or "positions",
     * or "" if nothing is parsed currently.
     */
    def parsedClosure = ""
    
    /** The name of the Migration group being parsed */
    def migrationParsed = ""
    
    /**
     * two variable is useful when we load many files of configuration Migration.
     * at first one part of each file which define the definition of MigrationItem will be loaded
     * one time we finish to load all definition of  Migration item from all files , we start to
     * load MigrationPosition
     */
    boolean blockLoadChangeSets,blockLoadChanges
    /**
     * Parses the Migration
     * 
     * @param Migration The Migration configuration to parse
     * 
     * @author Antoine Roux
     */
    void parse(Closure migration) {
        migration.delegate = this
        migration.call()
    }
	
	
	void changeSet(Closure changeset) {
		if (blockLoadChangeSets) return
			parsedClosure = "changeSet"
		changeset.delegate = this
		changeset.call()
		parsedClosure = ""
	}
	
	
    /**
     * load all the definition of Migration from all configuration Migration file 
     * @author  Jun Chen 
     * @param MigrationClassList list of configuration Migration files
     */
    def loadAll(def migrationClassList) {
        migrations = [:]  
        //positions = []
        //migrationItems = [:] 
        parsedClosure = "" 
        migrationParsed = ""
        blockLoadChangeSets=false
        blockLoadChanges=true
        migrationClassList.each {
            if (it.migration)
                parse it.migration
        }
        blockLoadChangeSets=true
        blockLoadChanges=false
        migrationClassList.each {
            if (it.migration)
                parse it.migration
        }
        sort()
    }
}
