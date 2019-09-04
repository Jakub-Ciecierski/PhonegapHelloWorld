#ifndef TripDBManager_h
#define TripDBManager_h

#import <Foundation/Foundation.h>
#import "sqlite3.h"

@interface TripDBManager : NSObject
    
typedef struct GPSData {
    int rowID;
    float distanceSoFar;
    float latitude;
    float longitude;
    double timestamp;
} GPSData;
  
+ (int) InvalidDBID;
    
@property (nonatomic, copy) NSMutableDictionary *appDBPaths;

+(sqlite3 *)db;

+ (instancetype)sharedInstance;

- (void) openDB;
- (void) closeDB;
- (void) write:(GPSData)gpsData;
- (void) clearTable;
    
- (GPSData) readLatestRow;


    
@end

#endif /* TripDBManager_h */
