#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

#ifndef LocationManager_h
#define LocationManager_h

@interface LocationManager : NSObject<CLLocationManagerDelegate>
@property (strong, nonatomic) CLLocationManager *locationManager;
@property (strong, nonatomic) NSDate *lastTimestamp;

+ (instancetype)sharedInstance;
- (void)startUpdatingLocation;
- (void)stopUpdatingLocation;
- (void)requestLocation;
@end

#endif /* LocationManager_h */

