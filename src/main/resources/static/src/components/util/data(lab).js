

export const eventTypeNameOptions = [
    { value: 'Person_Leave', label: 'Person_Leave' },
    { value: 'Person_Entry', label: 'Person_Entry' },
    { value: 'Printer_Finish', label: 'Printer_Finish' },
    { value: 'Printer_Start', label: 'Printer_Start' },
    { value: 'Light_Off', label: 'Light_Off' },
    { value: 'Light_On', label: 'Light_On' },
    { value: 'Fire_Alarm', label: 'Fire_Alarm' },
    { value: 'Temperature_Change', label: 'Temperature_Change' },

    { value: 'Window_Off', label: 'Window_Off' },
    { value: 'Window_On', label: 'Window_On' },
    { value: 'Door_Off', label: 'Door_Off' },
    { value: 'Door_On', label: 'Door_On' },
    { value: 'AC_Off', label: 'AC_Off' },
    { value: 'AC_On', label: 'AC_On' },
    { value: 'Oven_Off', label: 'Oven_Off' },
    { value: 'Oven_On', label: 'Oven_On' },
    { value: 'Water_Dispenser_Off', label: 'Water_Dispenser_Off' },
    { value: 'Water_Dispenser_On', label: 'Water_Dispenser_On' },
]
// eventType(18): Person_Leave, Person_Entry, Printer_Finish, Printer_Start, Light_Off, Light_On, Fire_Alarm, Temperature_Change, Window_Off, Window_On, Door_Off, Door_On, AC_Off, AC_On, Oven_Off, Oven_On, Water_Dispenser_Off, Water_Dispenser_On

export const actionOptions = [
    { value: 'AC_Turn_On', label: 'AC_Turn_On' },
    { value: 'Send_Message', label: 'Send_Message' },
    { value: 'Speak_VoiceMessage', label: 'Speak_VoiceMessage' },
    { value: 'AC_Turn_Off', label: 'AC_Turn_Off' },
    { value: 'Light_Turn_Off', label: 'Light_Turn_Off' },
    { value: 'Light_Turn_On', label: 'Light_Turn_On' },
    { value: 'Clean_Air', label: 'Clean_Air' },
    { value: 'Humidify_Air', label: 'Humidify_Air' },
]
// action(8): AC_Turn_On, Send_Message, Speak_VoiceMessage, AC_Turn_Off, Light_Turn_Off, Light_Turn_On, Clean_Air, Humidify_Air



export const locationOptions = [
    { value: 'corridor01', label: 'corridor01' },
    { value: 'meetingroom02', label: 'meetingroom02' },
    { value: 'meetingroom01', label: 'meetingroom01' },
    { value: 'tearoom01', label: 'tearoom01' },
    { value: 'laboratory01', label: 'laboratory01' },
    { value: 'trigger_location', label: 'trigger_location' }
]


export const eventDataOptions = [
    { value: 'location', label: 'location' },
    { value: 'printer_work_state', label: 'printer_work_state' },
    { value: 'current_light_state', label: 'current_light_state' },
    { value: 'temperature', label: 'temperature' },
]

const deviceInLocation = [
    {
        value: 'speaker', label: 'speaker', children: [
            { value: 'volume', label: 'volume' },
        ],
    },
    {
        value: 'printer', label: 'printer', children: [
            { value: 'printer_work_state', label: 'printer_work_state' },
        ],
    },
    {
        value: 'window', label: 'window', children: [
            { value: 'current_window_state', label: 'current_window_state' },
        ],
    },
    {
        value: 'water_dispenser', label: 'water_dispenser', children: [
            { value: 'water_dispenser_working_state', label: 'water_dispenser_working_state' },
        ],
    },
    {
        value: 'oven', label: 'oven', children: [
            { value: 'oven_working_state', label: 'oven_working_state' },
        ],
    },
    {
        value: 'ac', label: 'ac', children: [
            { value: 'current_ac_state', label: 'current_ac_state' },
        ],
    },
    {
        value: 'light', label: 'light', children: [
            { value: 'current_light_state', label: 'current_light_state' },
        ],
    }
]
// deviceInLocation(7): speaker, printer, window, water_dispenser, oven, ac, light


export const LocationPropertyOptions = [
    { value: 'temperature', label: 'temperature' },
    { value: 'people_count', label: 'people_count' },
    { value: 'luminance', label: 'luminance' },
    { value: 'any', label: 'any', children: deviceInLocation },
    { value: 'all', label: 'all', children: deviceInLocation },
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
    { value: 'security check', label: 'security check' },

]

export const locationPrepositionOptions = [
    { value: 'is', label: 'is' },
    { value: 'contain', label: 'contain' },
    { value: 'adjacentTo', label: 'adjacentTo' },
    { value: 'reachableTo', label: 'reachableTo' }
]

export const objectIdOptions = [
    { value: 'trigger_objectId', label: 'trigger_objectId' },
    { value: 'security', label: 'security' },
]

export const functionOptions = [
    { value: 'count', label: 'count' },
    { value: 'time', label: 'time' },
    { value: 'seq_match', label: 'seq_match' },
]






export const comparatorOptions = [
    { value: '>', label: '>' },
    { value: '<', label: '<' },
    { value: '=', label: '=' },
]

export const logicalOperatorOptions = [
    { value: 'and', label: 'and' },
    { value: 'or', label: 'or' },
]

export const objectIdOperatorOptions = [
    { value: 'is', label: 'is' },
    { value: 'isNot', label: 'isNot' },
    { value: 'belongsTo', label: 'belongsTo' },
    { value: 'notBelongsTo', label: 'notBelongsTo' },
]

