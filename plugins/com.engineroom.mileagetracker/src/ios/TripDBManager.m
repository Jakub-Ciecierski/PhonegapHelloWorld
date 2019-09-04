#import "TripDBManager.h"

@implementation TripDBManager

@synthesize appDBPaths;
static sqlite3* db = 0;

static int INVALID_DB_ID = 0;
    
+ (int)InvalidDBID{
    return INVALID_DB_ID;
}
    
+ (instancetype)sharedInstance
{
    static id sharedInstance = nil;
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    NSLog(@"TripDBManager::sharedInstance");
    return sharedInstance;
}

-(void) openDB{
    printf("TripDBManager - openDB\n");
    
    [self sqliteInit];
    [self sqliteOpen];
    [self createTable];
}

- (void) sqliteInit {
    appDBPaths = [NSMutableDictionary dictionaryWithCapacity:0];
    
    NSString *docs = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex: 0];
    
    [appDBPaths setObject: docs forKey:@"docs"];
    
    NSString *libs = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex: 0];
    
    [appDBPaths setObject: libs forKey:@"libs"];
    
    NSString *nosync = [libs stringByAppendingPathComponent:@"LocalDatabase"];
    NSError *err;
    if ([[NSFileManager defaultManager] fileExistsAtPath: nosync])
    {
        [appDBPaths setObject: nosync forKey:@"nosync"];
    }
    else
    {
        if ([[NSFileManager defaultManager] createDirectoryAtPath: nosync withIntermediateDirectories:NO attributes: nil error:&err])
        {
            NSURL *nosyncURL = [ NSURL fileURLWithPath: nosync];
            if (![nosyncURL setResourceValue: [NSNumber numberWithBool: YES] forKey: NSURLIsExcludedFromBackupKey error: &err])
            {
            }
            [appDBPaths setObject: nosync forKey:@"nosync"];
        }
        else
        {
            [appDBPaths setObject: libs forKey:@"nosync"];
        }
    }
}

- (void) sqliteOpen {
    NSString *dbfilename = @"TripDB.db";
    NSString *dblocation = @"nosync";
    if (dblocation == NULL) dblocation = @"docs";
    
    NSString *dbname = [self getDBPath:dbfilename at:dblocation];
    const char *name = [dbname UTF8String];
    printf("TripDBManager - open full db path: %s\n", name);
    
    if (sqlite3_open(name, &db) != SQLITE_OK) {
        printf("TripDBManager - open db - failure\n");
    } else {
        sqlite3_db_config(db, SQLITE_DBCONFIG_DEFENSIVE, 1, NULL);
        // Attempt to read the SQLite master table [to support SQLCipher version]:
        if(sqlite3_exec(db, (const char*)"SELECT count(*) FROM sqlite_master;", NULL, NULL, NULL) == SQLITE_OK) {
            printf("TripDBManager - open db - Database opened\n");
        } else {
            printf("TripDBManager - open db - Unable to open DB with key\n");
        }
    }
}

- (void) createTable {
    printf("TripDBManager - createTable\n");
    const char *insert_stmt = "CREATE TABLE IF NOT EXISTS gps_table (id integer primary key, distance double, lat double, long double, timestamp double)";
    
    sqlite3_stmt *insert_statement;
    sqlite3_prepare_v2(db, insert_stmt, -1, &insert_statement, NULL);
    
    printf("TripDBManager - createTable - sqlite3_prepare_v2\n");
    
    if (sqlite3_step(insert_statement) == SQLITE_DONE)
    {
        printf("TripDBManager - createTable sqlite3_prepare_v2 - success\n");
    }else{
        printf("TripDBManager - createTable sqlite3_prepare_v2 - failure\n");
    }
    sqlite3_finalize(insert_statement);
}
    
-(id) getDBPath:(NSString *)dbFile at:(NSString *)atkey {
    if (dbFile == NULL) {
        return NULL;
    }
    
    NSString *dbdir = [appDBPaths objectForKey:atkey];
    NSString *dbPath = [dbdir stringByAppendingPathComponent: dbFile];
    return dbPath;
}

- (void) closeDB{
    printf("TripDBManager closeDB\n");
    sqlite3_close(db);
}

- (void) write:(GPSData)gpsData{
    printf("TripDBManager - write\n");
    const char *insert_stmt = "INSERT INTO gps_table (distance, lat, long, timestamp) VALUES (?,?,?,?)";
    
    sqlite3_stmt *insert_statement;
    sqlite3_prepare_v2(db, insert_stmt, -1, &insert_statement, NULL);
    sqlite3_bind_double(insert_statement, 1, gpsData.distanceSoFar);
    sqlite3_bind_double(insert_statement, 2, gpsData.latitude);
    sqlite3_bind_double(insert_statement, 3, gpsData.longitude);
    sqlite3_bind_double(insert_statement, 4, gpsData.timestamp);
    
    printf("TripDBManager - write - sqlite3_prepare_v2\n");
    
    if (sqlite3_step(insert_statement) == SQLITE_DONE)
    {
        printf("TripDBManager - write - sqlite3_prepare_v2 - insert success\n");
    }else{
        printf("TripDBManager - write - sqlite3_prepare_v2 - insert failure\n");
    }
    sqlite3_finalize(insert_statement);
}

- (GPSData) readLatestRow{
    const char *select_stmt = "SELECT * FROM gps_table WHERE id=(SELECT MAX(id) FROM gps_table)";
    sqlite3_stmt *select_statement;
    GPSData gpsData;
    gpsData.rowID = INVALID_DB_ID;
    
    if (sqlite3_prepare_v2(db, select_stmt, -1, &select_statement, NULL) == SQLITE_OK) {
    printf("TripDBManager - readLatestRow - sqlite3_prepare_v2\n");

    while (sqlite3_step(select_statement) == SQLITE_ROW) {
        gpsData.rowID  = sqlite3_column_int(select_statement, 0);
        gpsData.distanceSoFar = sqlite3_column_double(select_statement, 1);
        gpsData.latitude = sqlite3_column_double(select_statement, 2);
        gpsData.longitude = sqlite3_column_double(select_statement, 3);
        gpsData.timestamp = sqlite3_column_double(select_statement, 4);

        printf("TripDBManager - sqlite3_step [id, distance, lat, long, timestamp] = [%d, %f, %f, %f, %f] \n",
        gpsData.rowID, gpsData.distanceSoFar, gpsData.latitude, gpsData.longitude, gpsData.timestamp);
    }

    sqlite3_finalize(select_statement);
    }
    else {
        printf("TripDBManager - readLatestRow - !sqlite3_prepare_v2\n");
    }
    
    return gpsData;
}
    
- (void) clearTable{
    printf("TripDBManager - clearTable\n");
    const char *insert_stmt = "DELETE FROM gps_table";
    
    sqlite3_stmt *insert_statement;
    sqlite3_prepare_v2(db, insert_stmt, -1, &insert_statement, NULL);
    
    printf("TripDBManager - clearTable - sqlite3_prepare_v2\n");
    
    if (sqlite3_step(insert_statement) == SQLITE_DONE)
    {
        printf("TripDBManager - clearTable - sqlite3_prepare_v2 - success\n");
    }else{
        printf("TripDBManager - clearTable - sqlite3_prepare_v2 - failure\n");
    }
    sqlite3_finalize(insert_statement);
}

@end
