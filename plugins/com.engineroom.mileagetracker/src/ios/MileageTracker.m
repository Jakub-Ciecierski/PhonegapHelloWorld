#import "MileageTracker.h"
#import "TripManager.h"

@implementation MileageTracker

- (void)greet:(CDVInvokedUrlCommand*)command
{
    NSString* name = [[command arguments] objectAtIndex:0];
    NSString* msg = [NSString stringWithFormat: @"Hello Boy, %@", name];
    
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];
    
    printf("HWPHello::greet");
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)startTrip:(CDVInvokedUrlCommand*)command
{
    NSString* trackingIntervalMS_String = [[command arguments] objectAtIndex:0];
    NSString* returnIntervalMS_String = [[command arguments] objectAtIndex:1];
    
    NSInteger trackingIntervalMS = [trackingIntervalMS_String integerValue];
    NSInteger returnIntervalMS = [returnIntervalMS_String integerValue];
    
    printf("startTrip - native [trackingIntervalMS, returnIntervalMS] = [%d, %d]", trackingIntervalMS, returnIntervalMS);
    NSString* msg = [NSString stringWithFormat: @"startTrip callback - plugins/src/ios"];
    
    [self startTripLogic];
    
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}
    
- (void)pauseTrip:(CDVInvokedUrlCommand*)command
{
    [self startTripLogic];
    
    NSString* msg = [NSString stringWithFormat: @"pauseTrip callback"];
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}
    
- (void)startTripLogic{
    printf("startTripLogic");
    TripManager* tripManager = [TripManager sharedInstance];
    [tripManager startTrip];
}

- (void)stopTrip:(CDVInvokedUrlCommand*)command
{
    printf("stopTrip - native");
    
    NSString* msg = [NSString stringWithFormat: @"stopTrip callback"];
    
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];

    TripManager* tripManager = [TripManager sharedInstance];
    [tripManager stopTrip];
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

@end
