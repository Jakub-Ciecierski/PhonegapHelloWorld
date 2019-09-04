#ifndef TripManager_h
#define TripManager_h

#import <Foundation/Foundation.h>

@interface TripManager : NSObject

+ (instancetype)sharedInstance;

- (void) startTrip;
- (void) pauseTrip;
- (void) stopTrip;

@end

#endif /* TripManager_h */
