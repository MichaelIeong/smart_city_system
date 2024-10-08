
export const eventTypeNameOptions = [
    { value: 'Noisy_Detected', label: 'Noisy_Detected' },
    { value: 'Water_Leak', label: 'Water_Leak' },
    { value: 'Order_Delivered', label: 'Order_Delivered' },
    { value: 'Coffee_Start_making', label: 'Coffee_Start_making' },
    { value: 'Coffee_Done', label: 'Coffee_Done' },
    { value: 'Make_Order', label: 'Make_Order' },
    { value: 'Check_Out', label: 'Check_Out' },
    { value: 'Person_Leave', label: 'Person_Leave' },
    { value: 'Person_Entry', label: 'Person_Entry' },
    { value: 'Temperature_Change', label: 'Temperature_Change' },
    { value: 'AC_Off', label: 'AC_Off' },
    { value: 'AC_On', label: 'AC_On' },
    { value: 'Window_Off', label: 'Window_Off' },
    { value: 'Window_On', label: 'Window_On' },
    { value: 'Light_Off', label: 'Light_Off' },
    { value: 'Light_On', label: 'Light_On' }
]
// eventType(14): Noisy_Detected, Water_Leak, Order_Delivered, Coffee_Start_making, Coffee_Done, Make_Order, Check_Out, Person_Leave, Person_Entry, Temperature_Change, AC_Off, AC_On, Window_Off, Window_On

export const actionOptions = [
    { value: 'Speak_VoiceMessage', label: 'Speak_VoiceMessage' },
    { value: 'Make_Coffee', label: 'Make_Coffee' },
    { value: 'Light_Turn_Off', label: 'Light_Turn_Off' },
    { value: 'Light_Turn_On', label: 'Light_Turn_On' },
    { value: 'AC_Turn_Off', label: 'AC_Turn_Off' },
    { value: 'AC_Turn_On', label: 'AC_Turn_On' },
    { value: 'Make_Drink', label: 'Make_Drink' },
    { value: 'Guiding', label: 'Guiding' },
    { value: 'Notify_Waiter', label: 'Notify_Waiter' }
]
// action(9): Speak_VoiceMessage, Make_Coffee, Light_Turn_Off, Light_Turn_On, AC_Turn_Off, AC_Turn_On, Make_Drink, Guiding, Notify_Waiter

export const locationOptions = [
    { value: 'trigger_location', label: 'trigger_location' },
    { value: 'DiningArea05', label: 'DiningArea05' },
    { value: 'DiningArea04', label: 'DiningArea04' },
    { value: 'DiningArea03', label: 'DiningArea03' },
    { value: 'DiningArea02', label: 'DiningArea02' },
    { value: 'DiningArea01', label: 'DiningArea01' },
    { value: 'WorkArea01', label: 'WorkArea01' },
    { value: 'LeisureArea01', label: 'LeisureArea01' },
    { value: 'EntranceArea02', label: 'EntranceArea02' },
    { value: 'EntranceArea01', label: 'EntranceArea01' },
    { value: 'DiningArea', label: 'DiningArea' },
    { value: 'WorkArea', label: 'WorkArea' },
    { value: 'LeisureArea', label: 'LeisureArea' },
    { value: 'EntranceArea', label: 'EntranceArea' }
]

export const eventDataOptions = [
    { value: 'location', label: 'location' },
    { value: 'temperature', label: 'temperature' }
]

// #待修改
const deviceInLocation = [
    {
        value: 'speaker',
label: 'speaker',
children: [
            { value: 'volume', label: 'volume' }
        ]
    },
    {
        value: 'window',
label: 'window',
children: [
            { value: 'current_window_state', label: 'current_window_state' }
        ]
    },
    {
        value: 'ac',
label: 'ac',
children: [
            { value: 'current_ac_state', label: 'current_ac_state' }
        ]
    },
    {
        value: 'light',
label: 'light',
children: [
            { value: 'current_light_state', label: 'current_light_state' }
        ]
    },
    {
        value: 'Coffee_Machine',
label: 'Coffee_Machine',
children: [
            { value: 'current_Coffee_Machine_state', label: 'current_Coffee_Machine_state' }
        ]
    },
    {
        value: 'Deliver_Robot',
label: 'Deliver_Robot',
children: [
            { value: 'current_Deliver_Robot_state', label: 'current_Deliver_Robot_state' }
        ]
    },
    {
        value: 'Guiding_Robot',
label: 'Guiding_Robot',
children: [
            { value: 'current_Guiding_Robot_state', label: 'current_Guiding_Robot_state' }
        ]
    }
]
// deviceInLocation(7): speaker, window, ac, light, Coffee_Machine, Deliver_Robot, Guiding_Robot

export const LocationPropertyOptions = [
    { value: 'temperature', label: 'temperature' },
    { value: 'people_count', label: 'people_count' },
    { value: 'luminance', label: 'luminance' },
    { value: 'any', label: 'any', children: deviceInLocation },
    { value: 'all', label: 'all', children: deviceInLocation }
]

export const appOptions = [
    { value: 'overwork', label: 'overwork' },
    { value: 'printer add paper', label: 'printer add paper' },
    { value: 'autolight', label: 'autolight' },
    { value: 'turn on AC', label: 'turn on AC' },
    { value: 'light overuse', label: 'light overuse' },
    { value: 'AC/Window conflict', label: 'AC/Window conflict' },
    { value: 'fast run', label: 'fast run' },
    { value: 'fire detect', label: 'fire detect' },
    { value: 'unusual behavior', label: 'unusual behavior' },
    { value: 'oven&water dispenser', label: 'oven&water dispenser' },
    { value: 'security check', label: 'security check' }

]

// ======================
export const locationPrepositionOptions = [
    { value: 'is', label: 'is' },
    { value: 'contain', label: 'contain' },
    { value: 'adjacentTo', label: 'adjacentTo' },
    { value: 'reachableTo', label: 'reachableTo' }
]

export const objectIdOptions = [
    { value: 'trigger_objectId', label: 'trigger_objectId' },
    { value: 'vip', label: 'vip' }
]

export const functionOptions = [
    { value: 'count', label: 'count' },
    { value: 'time', label: 'time' },
    { value: 'seq_match', label: 'seq_match' }
]

export const comparatorOptions = [
    { value: '>', label: '>' },
    { value: '<', label: '<' },
    { value: '=', label: '=' }
]

export const logicalOperatorOptions = [
    { value: 'and', label: 'and' },
    { value: 'or', label: 'or' }
]

export const objectIdOperatorOptions = [
    { value: 'is', label: 'is' },
    { value: 'isNot', label: 'isNot' },
    { value: 'belongsTo', label: 'belongsTo' },
    { value: 'notBelongsTo', label: 'notBelongsTo' }
]
