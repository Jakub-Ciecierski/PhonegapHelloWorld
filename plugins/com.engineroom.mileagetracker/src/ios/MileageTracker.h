#import <Cordova/CDV.h>

@interface MileageTracker : CDVPlugin

- (void) greet:(CDVInvokedUrlCommand*)command;
- (void) startTrip:(CDVInvokedUrlCommand*)command;
- (void) pauseTrip:(CDVInvokedUrlCommand*)command;
- (void) stopTrip:(CDVInvokedUrlCommand*)command;

@end

