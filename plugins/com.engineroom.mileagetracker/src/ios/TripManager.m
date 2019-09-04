#import "TripManager.h"
#import "TripDBManager.h"
#import "LocationManager.h"

@implementation TripManager

+ (instancetype)sharedInstance
{
    static id sharedInstance = nil;
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    NSLog(@"TripManager::sharedInstance");
    return sharedInstance;
}

- (void) startTrip{
    NSLog(@"TripManager::startTrip");
    
    TripDBManager* tripDBManager = [TripDBManager sharedInstance];
    [tripDBManager openDB];
    
    LocationManager* locationManager = [LocationManager sharedInstance];
    [locationManager startUpdatingLocation];
}
    
- (void) pauseTrip{
    NSLog(@"TripManager::pauseTrip");
    
    TripDBManager* tripDBManager = [TripDBManager sharedInstance];
    [tripDBManager closeDB];
    
    LocationManager* locationManager = [LocationManager sharedInstance];
    [locationManager stopUpdatingLocation];
}
    
- (void) stopTrip{
    NSLog(@"TripManager::stopTrip");
    
    TripDBManager* tripDBManager = [TripDBManager sharedInstance];
    [tripDBManager clearTable];
    [tripDBManager closeDB];
    
    LocationManager* locationManager = [LocationManager sharedInstance];
    [locationManager stopUpdatingLocation];
}

@end
