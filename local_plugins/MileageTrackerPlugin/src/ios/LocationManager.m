#import "LocationManager.h"
#import "TripDBManager.h"

@implementation LocationManager

+ (instancetype)sharedInstance
{
    static id sharedInstance = nil;
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
        LocationManager *instance = sharedInstance;
        instance.locationManager = [CLLocationManager new];
        instance.locationManager.delegate = instance;
        instance.locationManager.desiredAccuracy = kCLLocationAccuracyBest; // you can use kCLLocationAccuracyHundredMeters to get better battery life
        instance.locationManager.pausesLocationUpdatesAutomatically = NO; // this is important
    });
    NSLog(@"LocationManager::sharedInstance");
    return sharedInstance;
}

- (void)startUpdatingLocation
{
    CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
    NSLog(@"LocationManager::startUpdatingLocation");
    if (status == kCLAuthorizationStatusDenied)
    {
        NSLog(@"LocationManager::Location services are disabled in settings.");
    }
    else
    {
        // for iOS 8
        if ([self.locationManager respondsToSelector:@selector(requestAlwaysAuthorization)])
        {
            [self.locationManager requestAlwaysAuthorization];
        }
        // for iOS 9
        if ([self.locationManager respondsToSelector:@selector(setAllowsBackgroundLocationUpdates:)])
        {
            [self.locationManager setAllowsBackgroundLocationUpdates:YES];
        }
        NSLog(@"LocationManager::startUpdatingLocation self.locationManager startUpdatingLocation.");
        [self.locationManager startUpdatingLocation];
    }
}

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    CLLocation *mostRecentLocation = locations.lastObject;
    NSLog(@"LocationManager::Current location: %@ %@", @(mostRecentLocation.coordinate.latitude), @(mostRecentLocation.coordinate.longitude));
    
    NSDate *now = [NSDate date];
    NSTimeInterval interval = self.lastTimestamp ? [now timeIntervalSinceDate:self.lastTimestamp] : 0;
    int writeIntervalInSeconds = 2;
    if (!self.lastTimestamp || interval >= writeIntervalInSeconds)
    {
        self.lastTimestamp = now;
        
        TripDBManager* tripDBManager = [TripDBManager sharedInstance];
        
        GPSData previousGPSData = [tripDBManager readLatestRow];
        GPSData newGPSData;
        printf("LocationManager - didUpdateLocations gpsData.rowID = [%d]\n", previousGPSData.rowID);
        
        newGPSData.rowID = previousGPSData.rowID + 1;
        newGPSData.latitude = mostRecentLocation.coordinate.latitude;
        newGPSData.longitude = mostRecentLocation.coordinate.longitude;
        newGPSData.timestamp = [now timeIntervalSince1970];
        
        if(previousGPSData.rowID == [TripDBManager InvalidDBID]){
            newGPSData.distanceSoFar = 0;
        }else{
            CLLocation *previousLocation = [[CLLocation alloc] initWithLatitude:previousGPSData.latitude longitude:previousGPSData.longitude];
            CLLocationDistance locationDistance = [mostRecentLocation distanceFromLocation:previousLocation];
            
            newGPSData.distanceSoFar = locationDistance + previousGPSData.distanceSoFar;
        }
        [tripDBManager write:newGPSData];
    }
}

- (void)stopUpdatingLocation{
    NSLog(@"LocationManager::stopUpdatingLocation");
    [self.locationManager stopUpdatingLocation];
}
    
- (void) requestLocation{
    NSLog(@"LocationManager::requestLocation");
    [self.locationManager requestLocation];
}

@end
